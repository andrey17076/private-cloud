package by.bsuir.csan.server.users;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class UsersManager implements Serializable {

    private static final String    ROOT_DIR_CREATED_MSG = "USERS FILES DIR CREATED";
    private static final File      rootDir = new File("root");
    private static final File      usersSaveFile = new File(rootDir.getPath() + "/users.save");
    private static ArrayList<User> users = new ArrayList<>();

    public static void loadUsersFromSave() {

        if (!rootDir.exists()) {
            rootDir.mkdir();
            saveUsersToFile();
            System.out.println(ROOT_DIR_CREATED_MSG);
        }

        try (FileInputStream  fin = new FileInputStream(usersSaveFile)) {
            ObjectInputStream oin = new ObjectInputStream(fin);
            users = (ArrayList<User>) oin.readObject();
            oin.close();
            fin.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void addUser(String login, String passHash) {
        User user = new User(login, passHash, rootDir.getPath());
        users.add(user);
        saveUsersToFile();
    }

    public static User getUser(String login, String passHash) {
        for (User user : users) {
            if (user.getLogin().equals(login)) {
                if (user.getPassHash().equals(passHash)) {
                    return user;
                }
            }
        }
        return null;
    }

    public static boolean isUserExists(String login) {
        boolean isExists = false;
        for (User u : users) {
            if (u.getLogin().equals(login)) {
                isExists = true;
                break;
            }
        }
        return isExists;
    }

    public static HashMap<File, String> getUserHashes(User user) {
        return user.getHashes();
    }

    public static void addFileTo(User user, File file) {
        user.addFile(file);
        saveUsersToFile();
    }

    public static String getUserDirPath(User user) {
        return user.getUserDirPath();
    }

    public static void deleteFileFrom(User user, String shortFilePath) {
        user.deleteFile(shortFilePath);
        saveUsersToFile();
    }

    public static File getFileFrom(User user, String shortFilePath) {
        return user.getFile(shortFilePath);
    }

    private static void saveUsersToFile() {
        try (FileOutputStream  fos = new FileOutputStream(usersSaveFile)) {
            ObjectOutputStream out = new ObjectOutputStream(fos);
            out.writeObject(users);
            fos.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
