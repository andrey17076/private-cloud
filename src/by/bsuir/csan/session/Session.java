package by.bsuir.csan.session;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public abstract class Session extends Thread {

    protected static final String CONNECT_MSG = "CONNECTED";
    protected static final String DISCONNECT_MSG = "DISCONNECTED";
    protected static final String OK_MSG = "OK";
    protected static final String NOT_AUTHORIZED_MSG = "YOU ARE NOT AUTHORIZED";
    protected static final String USER_EXISTS_MSG = "USER WITH THIS LOGIN IS ALREADY EXISTS";
    protected static final String USER_NOT_EXISTS_MSG = "USER WITH THIS LOGIN IS NOT EXISTS";
    protected static final String WRONG_PASSWORD_MSG = "WRONG PASSWORD";
    protected static final String COMMAND_MISSING_MSG = "INCORRECT COMMAND";
    protected static final String UNKNOWN_MESSAGE_MSG = "UNKNOWN MESSAGE";
    protected static final String NOT_FOUND_MSG = "NOT FOUND";
    protected static final String START_LOADING_MSG = "LOADING STARTS";

    protected static final String SIGN_CMD = "SIGN";
    protected static final String AUTH_CMD = "AUTH";
    protected static final String HASH_CMD = "HASH";
    protected static final String STORE_CMD = "STORE";
    protected static final String RETR_CMD = "RETR";
    protected static final String DEL_CMD = "DEL";
    protected static final String CHECK_CMD = "CHECK";
    protected static final String QUIT_CMD = "QUIT";


    protected static final int BUFFER_SIZE = 1024;
    protected enum LogType {TO, FROM}
    protected byte[] buffer = new byte[BUFFER_SIZE];

    protected Socket socket;
    protected FileOutputStream logFileStream;

    protected DataInputStream dataInputStream;
    protected DataOutputStream dataOutputStream;

    protected abstract void handleSession() throws IOException;

    public Session(Socket socket, File logFile) throws IOException {
        this.socket = socket;
        this.dataInputStream = new DataInputStream(socket.getInputStream());
        this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
        this.logFileStream = new FileOutputStream(logFile, true);
    }

    protected void log(String logMessage, LogType type) {
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

    protected void setLogFile(File logFile) {

        File parent = logFile.getParentFile();
        parent.mkdir();

        try {
            logFileStream = new FileOutputStream(logFile, true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    protected String receiveMessage() throws IOException {

        while (dataInputStream.available() == 0);

        String textMessage;
        int messageLength = dataInputStream.readInt();
        if (messageLength > 0) {
            byte[] message = new byte[messageLength];
            dataInputStream.readFully(message, 0, messageLength);
            textMessage = new String(message);
        } else {
            textMessage = UNKNOWN_MESSAGE_MSG;
        }

        log(textMessage, LogType.FROM);
        return textMessage;
    }

    protected void sendMessage(String message) throws IOException {
        dataOutputStream.writeInt(message.length());
        dataOutputStream.write(message.getBytes());
        dataOutputStream.flush();
        log(message, LogType.TO);
    }

    protected String getResponse(String request) throws IOException {
        sendMessage(request);
        return receiveMessage();
    }

    protected void sendFile(File file) throws IOException {
        FileInputStream fin = new FileInputStream(file);

        int length;
        while ((length = fin.read(buffer)) > 0) {
            dataOutputStream.write(buffer, 0, length);
            dataOutputStream.flush();
        }

        fin.close();
    }

    protected File receiveFile(File file) throws IOException {

        while (dataInputStream.available() == 0);

        file.getParentFile().mkdir();
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

    protected void sendFilesHashes(HashMap<File, String> filesHashes) throws IOException {

        ObjectOutputStream oos = new ObjectOutputStream(dataOutputStream);
        oos.writeObject(filesHashes);
        oos.flush();

    }

    protected HashMap<File, String> receiveFilesHashes() throws IOException, ClassNotFoundException {

        while (dataInputStream.available() == 0);

        ObjectInputStream ois = new ObjectInputStream(dataInputStream);
        HashMap<File, String> filesHashes = (HashMap<File, String>) ois.readObject();

        return filesHashes;
    }

    @Override
    public void run() {
        try {
            handleSession();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                dataInputStream.close();
                dataOutputStream.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
