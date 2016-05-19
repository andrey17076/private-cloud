package by.bsuir.csan.server;

import by.bsuir.csan.server.user.User;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {

    private static ArrayList<User> users = new ArrayList<>();

    private static final File rootDir = new File("root");
    private static final File usersInfo = new File(rootDir + "/users.info");

    private static final String START_MSG = "SERVER START";
    private static final String ROOT_DIR_CREATED_MSG = "ROOT DIR CREATED";

    private static final int serverPort = 8888;

    private static void log(String message) {
        System.out.println(message);
    }

    private static void refreshUsersInfo() throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(usersInfo));
        out.writeObject(users);
    }

    public static File getRootDir() {
        return rootDir;
    }

    public static void putUser(User user) {
        users.add(user);
        File userDir = user.getUserDir();
        if (!userDir.exists()) {
            userDir.mkdir();
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        ServerSocket serverSocket = new ServerSocket(serverPort);
        log(START_MSG);

        if (!rootDir.exists()) {
            rootDir.mkdir();
            refreshUsersInfo();
            log(ROOT_DIR_CREATED_MSG);
        }

        ObjectInputStream in = new ObjectInputStream(new FileInputStream(usersInfo));
        users = (ArrayList<User>) in.readObject();

        do {
            Socket clientSocket = serverSocket.accept();
            ServerSession serverSession = new ServerSession(clientSocket);
            new Thread(serverSession).start();
        } while (true); //TODO replace with correct completion

        //serverSocket.close();
    }
}
