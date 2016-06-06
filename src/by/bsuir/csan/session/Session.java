package by.bsuir.csan.session;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

abstract class Session extends Thread {

    static final String         OK_MSG = "OK";
    static final String         NOT_AUTHORIZED_MSG = "YOU ARE NOT AUTHORIZED";
    static final String         USER_EXISTS_MSG = "USER WITH THIS LOGIN IS ALREADY EXISTS";
    static final String         USER_NOT_EXIST_MSG = "USER WITH THIS LOGIN AND PASSWORD IS NOT EXIST";
    static final String         COMMAND_MISSING_MSG = "INCORRECT COMMAND";
    static final String         START_LOADING_MSG = "LOADING STARTED";
    static final String         SIGN_CMD = "SIGN";
    static final String         AUTH_CMD = "AUTH";

    static final String         HASH_CMD = "HASH";
    static final String         STORE_CMD = "STORE";
    static final String         RETR_CMD = "RETR";
    static final String         DEL_CMD = "DEL";
    static final String         QUIT_CMD = "QUIT";

    private static final int    BUFFER_SIZE = 1024;
    private static final String CONNECT_MSG = "CONNECTED";
    private static final String DISCONNECT_MSG = "DISCONNECTED";

    private byte[]              buffer = new byte[BUFFER_SIZE];

    enum LogType                {TO, FROM}

    private Socket              socket;
    private DataInputStream     dataInputStream;
    private DataOutputStream    dataOutputStream;
    private FileOutputStream    logFileStream;
    private boolean             sessionInActiveState;

    protected abstract void handleSessionPermanently();

    Session(Socket socket, File logFile) throws IOException {
        this.socket = socket;
        this.dataInputStream = new DataInputStream(socket.getInputStream());
        this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
        this.logFileStream = new FileOutputStream(logFile, true);
        this.sessionInActiveState = true;
        log(CONNECT_MSG, LogType.FROM);
    }

    public void closeSession() {
        try {
            dataInputStream.close();
            dataOutputStream.close();
            socket.close();
            sessionInActiveState = false;
            log(DISCONNECT_MSG, LogType.FROM);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (sessionInActiveState) {
            handleSessionPermanently();
        }
        closeSession();
    }

    String receiveMessage() {
        String textMessage = (String) receive();
        log(textMessage, LogType.FROM);
        return textMessage;
    }

    void sendMessage(String message) {
        send(message);
        log(message, LogType.TO);
    }

    String getResponse(String request) {
        sendMessage(request);
        return receiveMessage();
    }

    void sendFile(File file) {
        try (FileInputStream fin = new FileInputStream(file)) {

            int length;
            while ((length = fin.read(buffer)) > 0) {
                dataOutputStream.write(buffer, 0, length);
                dataOutputStream.flush();
            }

            fin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    File receiveFile(File file) {
        try (FileOutputStream fos = new FileOutputStream(file, false)) {

            file.getParentFile().mkdirs();

            while (dataInputStream.available() == 0);

            int length;
            while (dataInputStream.available() > 0) {
                length = dataInputStream.read(buffer, 0, BUFFER_SIZE);
                fos.write(buffer, 0, length);
                fos.flush();
            }
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    void sendFilesHashes(HashMap<File, String> filesHashes) {
        send(filesHashes);
    }

    HashMap<File, String> receiveFilesHashes() {
        return (HashMap<File, String>) receive();
    }

    private Object receive() {
        Object receivedObject = null;
        try {
            while (dataInputStream.available() == 0);
            ObjectInputStream ois = new ObjectInputStream(dataInputStream);
            receivedObject = ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return receivedObject;
    }

    private void send(Object object) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(dataOutputStream);
            oos.writeObject(object);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void log (String logMessage, LogType type) {
        String timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime());
        String address = socket.getInetAddress().getHostAddress();
        logMessage = "[" + timeStamp + "]: " + type.name() + " " + address + ": " + logMessage + "\n";
        try {
            logFileStream.write(logMessage.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
