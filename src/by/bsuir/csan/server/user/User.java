package by.bsuir.csan.server.user;

import by.bsuir.csan.server.Server;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class User implements Serializable {

    private String login;
    private String password; //TODO replace with pass hash
    private File userDir;
    private ArrayList<Integer> hashes = new ArrayList<>();
    private HashMap<Integer, File> userFiles = new HashMap<>();

    public User(String login, String password) {
        this.login = login;
        this.password = password;
        this.userDir = new File(Server.getRootDir() + "/" + Integer.toString(login.hashCode()));
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public File getUserDir() {
        return userDir;
    }

    public void putFile(File file) {
        int hash = file.hashCode();
        hashes.add(hash);
        userFiles.put(hash, file);
    }

    public ArrayList<Integer> getHashes() {
        return hashes;
    }
}
