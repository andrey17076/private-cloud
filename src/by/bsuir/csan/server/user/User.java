package by.bsuir.csan.server.user;

import by.bsuir.csan.server.Server;

import java.io.File;
import java.util.HashMap;

public class User {

    private String login;
    private String password; //TODO replace with pass hash
    private File userDir;
    private HashMap<Integer, File> userFiles = new HashMap<>();

    public User(String login, String password) {
        this.login = login;
        this.password = password;
        this.userDir = new File(Server.getRootDir() + "/" + Integer.toString(login.hashCode()));
    }

    public String getLogin() {
        return login;
    }

    public File getUserDir() {
        return userDir;
    }

    public void putFile(File file) {
        userFiles.put(file.hashCode(), file);
    }
}
