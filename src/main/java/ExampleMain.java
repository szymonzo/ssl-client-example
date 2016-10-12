import java.io.File;
import java.net.URL;


public class ExampleMain {


    public static void main(String[] args) throws Exception {
        URL url = new URL("https://www.google.pl");
        ConnectionChecker connectionChecker = new ConnectionChecker(url, new File("keystore"), new File("truststore"), "changeit","changeit2", "GET").enableSslDebug(false);
        connectionChecker.checkConnection();
}
}