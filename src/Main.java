import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Created by szymon on 22.06.16.
 */
public class Main {


    public static void main(String[] args) throws Exception{
        System.out.println(LocalDateTime.now());
        System.setProperty("javax.net.debug","all");
        String password = "changeit";
        InputStream keyStoreUrl = new FileInputStream("keystore.jks");
        InputStream trustStoreUrl = new FileInputStream("keystore.jks");

        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(keyStoreUrl, password.toCharArray());
        KeyManagerFactory keyManagerFactory =
                KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, password.toCharArray());

        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(trustStoreUrl, password.toCharArray());
        TrustManagerFactory trustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);
        final SSLContext sslContext = SSLContext.getInstance("TLSv1.1");
        sslContext.init(keyManagerFactory.getKeyManagers(),
                trustManagerFactory.getTrustManagers(),
                new SecureRandom());
        SSLContext.setDefault(sslContext);

        URL url = new URL("https://services.mastercard.com/virtual/mdes/digitization/1/0/211653/checkEligibility");
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type",
                "application/json;charset=utf-8");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Accept-Charset", "UTF-8");
        connection.setDoInput(true);
        connection.setDoOutput(true);
        System.out.println(connection.getResponseCode());

        BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getErrorStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        System.out.println(response.toString());

    }
}
