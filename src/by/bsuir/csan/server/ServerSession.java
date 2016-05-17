package by.bsuir.csan.server;

import java.io.*;
import java.net.Socket;

public class ServerSession extends Thread {
    
    private Socket clientSocket;
    private DataInputStream socketInStream;
    private DataOutputStream socketOutStream;

    public ServerSession(Socket socket) throws IOException {
        this.clientSocket = socket;
        this.socketInStream = new DataInputStream(socket.getInputStream());
        this.socketOutStream = new DataOutputStream(socket.getOutputStream());
        log("Client connected");
    }

    private void log(String logMessage) {
        System.out.println("From " + clientSocket.getInetAddress().getHostAddress() + ": " + logMessage);
    }

    private void handleClient() throws IOException {
        int messageLength;
        String textMessage = null;

        do {
            messageLength = socketInStream.readInt();
            if (messageLength > 0) {
                byte[] message = new byte[messageLength];
                socketInStream.readFully(message, 0, messageLength);
                textMessage = new String(message);
                log(textMessage);
            }
        } while (!textMessage.equals("QUIT"));
    }

    @Override
    public void run() {
        try {
            handleClient();
            log("Client disconnected");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
