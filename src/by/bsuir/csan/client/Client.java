package by.bsuir.csan.client;

import by.bsuir.csan.session.ClientSession;

import java.io.*;
import java.net.Socket;

public class Client {

    private static final int serverPort = 8888;
    private static final String serverIP = "10.211.55.21"; //TODO replace with inetaddr chooser

    private Socket socket;
    private static File rootDir = new File("_root"); //TODO replace with custom dir
    private boolean overrideOption = true; //TODO user must choose

    public Client() throws IOException {
        socket = new Socket(serverIP, serverPort);

        if (!rootDir.exists()) {
            rootDir.mkdir();
        }
    }

    protected void setOverrideOption(boolean option) {
        overrideOption = option;
    }

    public boolean hasOverrideOption() {
        return overrideOption;
    }

    public Socket getSocket() {
        return socket;
    }

    public File getRootDir() {
        return rootDir;
    }

    public static void main(String[] args) throws IOException {

        Client client = new Client();
        ClientSession clientSession = new ClientSession(client);

        String username = "user";
        String password = "pass";

        clientSession.signUp(username, password);
        clientSession.authorize(username, password);
        clientSession.checkAuthorization();

//        clientSession.quit();

        new Thread(clientSession).start();
    }
}
