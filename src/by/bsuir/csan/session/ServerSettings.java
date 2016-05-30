package by.bsuir.csan.session;

public class ServerSettings {

    private static final String serverIP = "10.211.55.21";
    private static final int serverPort = 8888;

    public static int getServerPort() {
        return serverPort;
    }

    public static String getServerIP() {
        return serverIP;
    }

}
