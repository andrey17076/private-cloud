package by.bsuir.csan.server;

import by.bsuir.csan.server.user.User;
import by.bsuir.csan.session.ServerSession;

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
    private static final String CANT_PUT_USER_MSG = "CAN'T PUT NEW USER INFO";

    private static final int serverPort = 8888;

    private static void log(String message) {
        System.out.println(message);
    }

    private static void saveUsersInfo() throws IOException {
        FileOutputStream fos = new FileOutputStream(usersInfo, false);
        ObjectOutputStream out = new ObjectOutputStream(fos);
        out.writeObject(users);
        fos.close();
        out.close();
    }

    private static void loadUsersInfo() throws IOException, ClassNotFoundException {
        FileInputStream fin = new FileInputStream(usersInfo);
        ObjectInputStream oin = new ObjectInputStream(fin);
        users = (ArrayList<User>) oin.readObject();
        oin.close();
        fin.close();
    }

    public static ArrayList<User> getUsers() {
        return users;
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

        try {
            saveUsersInfo();
        } catch (IOException e) {
            log(CANT_PUT_USER_MSG);
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        ServerSocket serverSocket = new ServerSocket(serverPort);
        log(START_MSG);

        if (!rootDir.exists()) {
            rootDir.mkdir();
            saveUsersInfo();
            log(ROOT_DIR_CREATED_MSG);
        }

        loadUsersInfo();

        do {
            Socket clientSocket = serverSocket.accept();
            ServerSession serverSession = new ServerSession(clientSocket);
            new Thread(serverSession).start();
        } while (true); //TODO replace with correct completion

        //serverSocket.close();
    }
}
