package by.bsuir.csan.server;

import by.bsuir.csan.server.users.UsersInfo;
import by.bsuir.csan.session.ServerSettings;
import by.bsuir.csan.session.ServerSession;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server extends Thread {

    private static final String START_MSG = "SERVER STARTS";
    private static final String FINISH_MSG = "SERVER FINISHED";
    private static final String START_ERR_MSG = "CAN'T RUN SERVER";
    private static final String FINISH_ERR_MSG = "CAN'T STOP SERVER";
    private static final String SHOW_ERR_MSG = "CAN'T SHOW LOG";
    private static final String STOP_SESSION_ERR_MSG = "CAN'T STOP SERVER SESSION";
    private static final String WRONG_COMMAND_MSG = "WRONG COMMAND";

    private static final String LOG_CMD = "LOG";
    private static final String QUIT_CMD = "QUIT";

    private static final File serverLog = new File("server.log");
    private static final int BUFFER_SIZE = 256;

    private ServerSocket serverSocket;
    private boolean isActive = true;
    private ArrayList<Socket> clientSockets = new ArrayList<>();
    private Thread listenerThread;

    private Server() {
        try {
            this.serverSocket = new ServerSocket(ServerSettings.getServerPort());
        } catch (IOException e) {
            System.out.println(START_ERR_MSG);
        }
    }

    private void printLog(){
        try {
            FileInputStream fis = new FileInputStream(serverLog);
            int length;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((length = fis.read(buffer)) > 0) {
                System.out.write(buffer, 0, length);
            }
        } catch (IOException e) {
            System.out.println(SHOW_ERR_MSG);
        }
    }

    private void runServer() {
        System.out.println(START_MSG);
        UsersInfo.loadUsersInfo();
        listenerThread = new Thread(this);
        listenerThread.start();

        while (isActive) {
            System.out.print(">");
            StringTokenizer tokenizer = new StringTokenizer(System.console().readLine());
            String command = tokenizer.nextToken();

            if (command.equals(LOG_CMD)) {
                printLog();
            } else if (command.equals(QUIT_CMD)) {
                stopServer();
            } else {
                System.out.println(WRONG_COMMAND_MSG);
            }
        }
    }

    private void stopServer() {
        isActive = false;
        clientSockets.forEach((socket) -> {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println(STOP_SESSION_ERR_MSG);
            }
        });
        listenerThread.interrupt();
        try {
            serverSocket.close();
            System.out.println(FINISH_MSG);
        } catch (IOException e) {
            System.out.println(FINISH_ERR_MSG);
        }
    }
    @Override
    public void run() {
        while (isActive) {
            try {
                Socket clientSocket = serverSocket.accept();
                clientSockets.add(clientSocket);
                ServerSession serverSession = new ServerSession(clientSocket, serverLog);
                Thread sessionThread = new Thread(serverSession);
                sessionThread.start();
            } catch (IOException e) {
                //Server socket closed
            }
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.runServer();
    }

}
