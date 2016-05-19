package by.bsuir.csan.client;

import by.bsuir.csan.session.ClientSession;

import java.io.*;
import java.net.Socket;

public class Client {

    private static final int serverPort = 8888;
    private static final String serverIP = "10.211.55.21"; //TODO replace with inetaddr chooser

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket(serverIP, serverPort);
        new Thread(new ClientSession(socket)).start();
    }
}
