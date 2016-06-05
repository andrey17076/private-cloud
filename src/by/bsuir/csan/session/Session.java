package by.bsuir.csan.session;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

abstract class Session extends Thread {

    private static final int BUFFER_SIZE = 1024;

    static final String CONNECT_MSG = "CONNECTED";
    static final String DISCONNECT_MSG = "DISCONNECTED";
    static final String OK_MSG = "OK";
    static final String NOT_AUTHORIZED_MSG = "YOU ARE NOT AUTHORIZED";
    static final String USER_EXISTS_MSG = "USER WITH THIS LOGIN IS ALREADY EXISTS";
    static final String USER_NOT_EXIST_MSG = "USER WITH THIS LOGIN IS NOT EXIST";
    static final String WRONG_PASSWORD_MSG = "WRONG PASSWORD";
    static final String COMMAND_MISSING_MSG = "INCORRECT COMMAND";
    static final String NOT_FOUND_MSG = "NOT FOUND";
    static final String START_LOADING_MSG = "LOADING STARTED";

    static final String SIGN_CMD = "SIGN";
    static final String AUTH_CMD = "AUTH";
    static final String HASH_CMD = "HASH";
    static final String STORE_CMD = "STORE";
    static final String RETR_CMD = "RETR";
    static final String DEL_CMD = "DEL";
    static final String CHECK_CMD = "CHECK";
    static final String QUIT_CMD = "QUIT";

    private byte[] buffer = new byte[BUFFER_SIZE];
    private FileOutputStream logFileStream;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    Socket socket;
    enum LogType {TO, FROM}

    protected abstract void handleSession() throws IOException;

    Session(Socket socket, File logFile) throws IOException {
        this.socket = socket;
        this.dataInputStream = new DataInputStream(socket.getInputStream());
        this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
        this.logFileStream = new FileOutputStream(logFile, true);
    }

    public void closeSession() {
        try {
            this.interrupt();
            dataInputStream.close();
            dataOutputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void log(String logMessage, LogType type) {
        String timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime());
        logMessage = "[" + timeStamp + "]: " +
                type.name() + " " +
                socket.getInetAddress().getHostAddress() + ": "
                + logMessage + "\n";
        try {
            logFileStream.write(logMessage.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    String receiveMessage() throws IOException {

        while (dataInputStream.available() == 0) ;

        int messageLength = dataInputStream.readInt();
        byte[] message = new byte[messageLength];
        dataInputStream.readFully(message, 0, messageLength);
        String textMessage = new String(message);

        log(textMessage, LogType.FROM);
        return textMessage;
    }

    void sendMessage(String message) throws IOException {
        dataOutputStream.writeInt(message.length());
        dataOutputStream.write(message.getBytes());
        dataOutputStream.flush();
        log(message, LogType.TO);
    }

    String getResponse(String request) throws IOException {
        sendMessage(request);
        return receiveMessage();
    }

    void sendFile(File file) throws IOException {
        FileInputStream fin = new FileInputStream(file);

        int length;
        while ((length = fin.read(buffer)) > 0) {
            dataOutputStream.write(buffer, 0, length);
            dataOutputStream.flush();
        }

        fin.close();
    }

    File receiveFile(File file) throws IOException {

        while (dataInputStream.available() == 0);

        file.getParentFile().mkdirs();
        file.createNewFile();

        FileOutputStream fos = new FileOutputStream(file, false);

        int length;
        while (dataInputStream.available() > 0) {
            length = dataInputStream.read(buffer, 0, BUFFER_SIZE);
            fos.write(buffer, 0, length);
            fos.flush();
        }
        fos.close();
        return file;
    }

    void sendFilesHashes(HashMap<File, String> filesHashes) throws IOException {

        ObjectOutputStream oos = new ObjectOutputStream(dataOutputStream);
        oos.writeObject(filesHashes);
        oos.flush();

    }

    HashMap<File, String> receiveFilesHashes() throws IOException, ClassNotFoundException {

        while (dataInputStream.available() == 0);

        ObjectInputStream ois = new ObjectInputStream(dataInputStream);

        return (HashMap<File, String>) ois.readObject();
    }

    @Override
    public void run() {
        try {
            handleSession();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeSession();
        }
    }
}
