package by.bsuir.csan.server.users;

import by.bsuir.csan.helpers.HashHelper;

import java.io.*;
import java.util.HashMap;
import java.util.Set;

public class UsersInfo implements Serializable {

    private static final String ROOT_DIR_CREATED_MSG = "USERS INFO DIR CREATED";

    private static final File rootDir = new File("root");
    private static final File usersInfoFile = new File(rootDir.getPath() + "/users.info");;
    private static HashMap<User, HashMap<File, String>> usersInfo = new HashMap<>();
    private static HashMap<String, File> usersLogs = new HashMap<>();

    private static void saveUsersInfo() {
        try {
            FileOutputStream fos = new FileOutputStream(usersInfoFile);
            ObjectOutputStream out = new ObjectOutputStream(fos);
            out.writeObject(usersInfo);
            out.writeObject(usersLogs);
            fos.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static HashMap<File, String> getUserInfo(User user) {
        return usersInfo.get(user);
    }

    public static Set<User> getUsers() {
        return usersInfo.keySet();
    }

    public static File getUserLog(String login) {
        return usersLogs.get(login);
    }

    public static File getUsersRootDir() {
        return rootDir;
    }

    public static void loadUsersInfo() {
        try {

            if (!rootDir.exists()) {
                rootDir.mkdir();
                saveUsersInfo();
                System.out.println(ROOT_DIR_CREATED_MSG);
            }

            FileInputStream fin = new FileInputStream(usersInfoFile);
            ObjectInputStream oin = new ObjectInputStream(fin);
            usersInfo = (HashMap<User, HashMap<File, String>>) oin.readObject();
            usersLogs = (HashMap<String, File>) oin.readObject();
            oin.close();
            fin.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void addUser(User user) {
        usersInfo.put(user, new HashMap<>());

        File userDir = user.getUserDir();
        if (!userDir.exists()) {
            userDir.mkdir();
            File userLog = new File(userDir + "/" + user.getLogin() + ".log");
            usersLogs.put(user.getLogin(), userLog);
        }
        saveUsersInfo();
    }

    public static void addFileTo(User user, File file) {
        String hash = HashHelper.getHash(file);
        String filePath = file.getPath().replaceFirst(user.getUserDir().getPath() + "/", "");
        usersInfo.get(user).put(new File(filePath), hash);
        saveUsersInfo();
    }

    public static void deleteFileFrom(User user, File file) {
        file.delete();
        String filePath = file.getPath().replaceFirst(user.getUserDir().getPath() + "/", "");
        usersInfo.get(user).remove(new File(filePath));
    }

    public static File getFileFrom(User user, String filePath) {
        if (usersInfo.get(user).containsKey(new File(filePath))) {
            String serverFilePath = user.getUserDir() + "/" + filePath;
            return new File(serverFilePath);
        }
        return null;
    }
}
