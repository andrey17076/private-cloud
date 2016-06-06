package by.bsuir.csan.server;

import by.bsuir.csan.server.users.UsersManager;
import by.bsuir.csan.sessions.ServerSettings;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server extends Thread {

    private static final String      START_MSG = "SERVER STARTS";
    private static final String      START_ERR_MSG = "CAN'T RUN SERVER";

    private static final String      LOG_CMD = "LOG";
    private static final String      STOP_CMD = "STOP";
    private static final String      WRONG_COMMAND_MSG = "WRONG COMMAND";
    private static final String      COMMAND_ERR = "CAN'T PERFORM COMMAND";

    private static final File        serverLog = new File("server.log");
    private static final int         BUFFER_SIZE = 256;

    private ServerSocket             serverSocket;
    private boolean                  serverInActiveState;
    private ArrayList<ServerSession> serverSessions;

    private Server() throws IOException {
        this.serverSocket = new ServerSocket(ServerSettings.getServerPort());
        this.serverInActiveState = true;
        this.serverSessions = new ArrayList<>();
    }

    public static void main(String[] args) {
        try {
            Server server = new Server();
            server.runServer();
        } catch (IOException e) {
            System.out.println(START_ERR_MSG);
        }
    }

    private static void printLog() throws IOException {
        FileInputStream fis = new FileInputStream(serverLog);
        byte[]          buffer = new byte[BUFFER_SIZE];
        int             length;
        while ((length = fis.read(buffer)) > 0) {
            System.out.write(buffer, 0, length);
        }
        fis.close();
    }

    @Override
    public void run() {
        while (serverInActiveState) {
            try {
                Socket        clientSocket = serverSocket.accept();
                ServerSession serverSession = new ServerSession(clientSocket, serverLog);
                serverSessions.add(serverSession); //need to close them all before server finish work
            } catch (IOException e) {
                //Server socket closed
            }
        }
    }

    private void runServer() {
        System.out.println(START_MSG);
        UsersManager.loadUsersFromSave();
        Thread listenerThread = new Thread(this);
        listenerThread.start();
        performDialog();
    }

    private void performDialog() {
        while (serverInActiveState) {
            System.out.print(">");
            String command = System.console().readLine();
            try {
                performCommand(command);
            } catch (IOException e) {
                System.out.println(COMMAND_ERR);
            }
        }
    }

    private void performCommand(String command) throws IOException {
        switch (command) {
            case LOG_CMD:
                printLog();
                break;
            case STOP_CMD:
                stopServer();
                break;
            default:
                System.out.println(WRONG_COMMAND_MSG);
                break;
        }
    }

    private void stopServer() throws IOException {
        serverInActiveState = false;
        serverSessions.forEach((session) -> session.closeSession());
        serverSocket.close();
    }
}
