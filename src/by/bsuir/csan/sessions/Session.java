package by.bsuir.csan.sessions;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public abstract class Session extends Thread {

    protected static final String DONE_MSG = "DONE";
    protected static final String NOT_AUTHORIZED_MSG = "YOU ARE NOT AUTHORIZED";
    protected static final String USER_EXISTS_MSG = "USER WITH THIS LOGIN IS ALREADY EXISTS";
    protected static final String USER_NOT_EXIST_MSG = "USER WITH THIS LOGIN AND PASSWORD IS NOT EXIST";
    protected static final String COMMAND_MISSING_MSG = "INCORRECT COMMAND";
    protected static final String START_LOADING_MSG = "LOADING STARTED";

    protected static final String SIGN_CMD = "SIGN";
    protected static final String AUTH_CMD = "AUTH";
    protected static final String HASH_CMD = "HASH";
    protected static final String STORE_CMD = "STORE";
    protected static final String RETR_CMD = "RETR";
    protected static final String DEL_CMD = "DEL";
    protected static final String QUIT_CMD = "QUIT";

    private static final int      BUFFER_SIZE = 1024;
    private static final String   CONNECT_MSG = "CONNECTED";
    private static final String   DISCONNECT_MSG = "DISCONNECTED";

    private Socket                socket;
    private DataInputStream       dataInputStream;

    private DataOutputStream      dataOutputStream;
    private FileOutputStream      logFileStream;
    private boolean               sessionInActiveState;

    private byte[]                buffer = new byte[BUFFER_SIZE];
    private enum LogType          {TO, FROM}

    protected Session(Socket socket, File logFile) throws IOException {
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

    protected abstract void handleSessionPermanently();

    protected String receiveMessage() {
        String textMessage = (String) receive();
        log(textMessage, LogType.FROM);
        return textMessage;
    }

    protected void sendMessage(String message) {
        send(message);
        log(message, LogType.TO);
    }

    protected String getResponse(String request) {
        sendMessage(request);
        return receiveMessage();
    }

    protected void sendFile(File file) {

        try {
            dataOutputStream.writeLong(file.length());
            FileInputStream fin = new FileInputStream(file);
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

    protected File receiveFile(File file) {
        file.getParentFile().mkdirs();

        try (FileOutputStream fos = new FileOutputStream(file, false)) {

            while (dataInputStream.available() == 0);

            long fileLength = dataInputStream.readLong();
            int length;
            while (fileLength > 0) {
                if ((dataInputStream.available() > fileLength) && (BUFFER_SIZE > fileLength)) {
                    length = dataInputStream.read(buffer, 0, (int) fileLength);
                } else {
                    length = dataInputStream.read(buffer, 0, BUFFER_SIZE);
                }
                fileLength -= length;
                fos.write(buffer, 0, length);
                fos.flush();
            }
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    protected void sendFilesHashes(HashMap<File, String> filesHashes) {
        send(filesHashes);
    }

    protected HashMap<File, String> receiveFilesHashes() {
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
