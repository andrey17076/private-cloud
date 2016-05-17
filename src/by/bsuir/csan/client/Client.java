package by.bsuir.csan.client;

import java.io.*;
import java.net.Socket;

public class Client {

    private static final int serverPort = 8888;
    private static DataOutputStream out;

    public void sendMessage(String message) {
        try {
            out.writeInt(message.length());
            out.write(message.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("10.211.55.21", serverPort); //TODO replace with inetaddr chooser
        out = new DataOutputStream(socket.getOutputStream());
        Client client = new Client();
        client.sendMessage("Bla");
        client.sendMessage("QUIT");
        socket.close();
    }
}
