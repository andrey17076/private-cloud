package by.bsuir.csan.session;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public abstract class Session extends Thread {

    protected final static String CONNECT_MSG = "CONNECTED";
    protected final static String DISCONNECT_MSG = "DISCONNECTED";
    protected final static String OK_MSG = "OK";
    protected final static String NOT_AUTHORIZED_MSG = "YOU ARE NOT AUTHORIZED";
    protected final static String COMMAND_MISSING_MSG = "INCORRECT COMMAND";

    protected final static String AUTH_CMD = "AUTH";
    protected final static String CHECK_CMD = "CHECK";
    protected final static String QUIT_CMD = "QUIT";

    protected enum LogType {TO, FROM}

    protected Socket socket;
    protected DataInputStream inStream;
    protected DataOutputStream outStream;

    protected abstract void handleSession() throws IOException;

    public Session(Socket socket) throws IOException {
        this.socket = socket;
        inStream = new DataInputStream(socket.getInputStream());
        outStream = new DataOutputStream(socket.getOutputStream());
    }

    protected void log(String logMessage) {
        System.out.println(logMessage);
    }

    protected void log(String logMessage, LogType type) {
        System.out.println(type.name() + " " + socket.getInetAddress().getHostAddress() + ": " + logMessage);
    }

    protected String receiveMessage() throws IOException {
        String textMessage = null;

        int messageLength = inStream.readInt();
        if (messageLength > 0) {
            byte[] message = new byte[messageLength];
            inStream.readFully(message, 0, messageLength);
            textMessage = new String(message);
        }

        return textMessage;
    }

    protected void sendMessage(String message) throws IOException {
        outStream.writeInt(message.length());
        outStream.write(message.getBytes());
        log(message, LogType.TO);
    }

    protected String getResponse(String request) throws IOException {
        sendMessage(request);
        return receiveMessage();
    }

    @Override
    public void run() {
        try {
            handleSession();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
