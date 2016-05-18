package by.bsuir.csan.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private static final String START_MSG = "SERVER START";
    private static final int serverPort = 8888;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(serverPort);
        System.out.println(START_MSG);

        do {
            Socket clientSocket = serverSocket.accept();
            ServerSession serverSession = new ServerSession(clientSocket);
            new Thread(serverSession).start();
        } while (true); //TODO replace with correct completion

        //serverSocket.close();
    }
}
