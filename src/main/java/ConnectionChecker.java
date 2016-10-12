import com.google.gson.*;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.net.URL;
import java.security.*;
import java.security.cert.CertificateException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


public class ConnectionChecker {

    private final String KEY_STORE_TYPE = "JKS";
    private final String PROTOCOL = "TLSv1.1";
    private final JsonParser JSON_PARSER = new JsonParser();
    private final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private URL url;
    private File keyStore;
    private File trustStore;
    private String keyStorePassword;
    private String trustStorePassword;
    private String requestMethod;

    public ConnectionChecker(URL url, File keyStore, File trustStore, String keyStorePassword, String trustStorePassword, String requestMethod) {
        this.url = url;
        this.keyStore = keyStore;
        this.trustStore = trustStore;
        this.keyStorePassword = keyStorePassword;
        this.trustStorePassword = trustStorePassword;
        this.requestMethod = requestMethod;
    }

    public ConnectionChecker enableSslDebug(boolean isEnabled){
         if(isEnabled) System.setProperty("javax.net.debug", "all");
        return this;
    }

    public void checkConnection() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, KeyManagementException {

        System.out.println("checking connection for environment " + url);
        System.out.println("TimeStamp : " + LocalDateTime.now());
        setUpConnection();
        HttpsURLConnection connection = prepareConnection();

        System.out.println("Connection response code : " + connection.getResponseCode());
        System.out.println("Printing headers : " + connection.getResponseCode());
        for (Map.Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
            System.out.println(header.getKey() + "=" + header.getValue());
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getResponseCode() == 200 ? connection.getInputStream() : connection.getErrorStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();


        try{
            //Try parsing to json
            JsonElement jsonElement = JSON_PARSER.parse(response.toString());
            String prettyJsonString = GSON.toJson(jsonElement);
            System.out.println(prettyJsonString);
        }catch (JsonSyntaxException ex){
            System.out.println(response);
        }finally {
            connection.disconnect();
        }



    }

    private HttpsURLConnection prepareConnection() throws IOException {
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod(this.requestMethod);
        connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Accept-Charset", "UTF-8");
        connection.setDoInput(true);
        connection.setDoOutput(true);
        return connection;
    }

    private void setUpConnection() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, KeyManagementException {
        InputStream keyStoreUrl = getFileInputStream(keyStore);
        InputStream trustStoreUrl = getFileInputStream(trustStore);
        KeyStore keyStore = loadCertificateStore(keyStoreUrl, this.keyStorePassword);
        KeyManagerFactory keyManagerFactory = InitializeKeyManagerFactory(keyStore, this.keyStorePassword);
        KeyStore trustStore = loadCertificateStore(trustStoreUrl, this.trustStorePassword);
        TrustManagerFactory trustManagerFactory = initializeTrustManagerFactory(trustStore);
        final SSLContext sslContext = SSLContext.getInstance(PROTOCOL);
        setUpSSLContext(keyManagerFactory, trustManagerFactory, sslContext);
    }

    private void setUpSSLContext(KeyManagerFactory keyManagerFactory, TrustManagerFactory trustManagerFactory, SSLContext sslContext) throws KeyManagementException {
        sslContext.init(keyManagerFactory.getKeyManagers(),
                trustManagerFactory.getTrustManagers(),
                new SecureRandom());
        SSLContext.setDefault(sslContext);
    }

    private TrustManagerFactory initializeTrustManagerFactory(KeyStore trustStore) throws NoSuchAlgorithmException, KeyStoreException {
        TrustManagerFactory trustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);
        return trustManagerFactory;
    }

    private KeyManagerFactory InitializeKeyManagerFactory(KeyStore keyStore, String password) throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
        KeyManagerFactory keyManagerFactory =
                KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, password.toCharArray());
        return keyManagerFactory;
    }

    private KeyStore loadCertificateStore(InputStream inputStreamStore, String password) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        KeyStore keyStore = KeyStore.getInstance(KEY_STORE_TYPE);
        keyStore.load(inputStreamStore, password.toCharArray());
        return keyStore;
    }

    private FileInputStream getFileInputStream(File file) throws FileNotFoundException {
        return new FileInputStream(file);
    }
}
