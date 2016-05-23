package by.bsuir.csan.server.user;

import by.bsuir.csan.helpers.HashHelper;
import by.bsuir.csan.server.Server;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class User implements Serializable {

    private String login;
    private String password; //TODO replace with pass hash
    private File userDir;
    private ArrayList<String> hashes = new ArrayList<>();
    private HashMap<String, File> userFiles = new HashMap<>();

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
        String hash = HashHelper.getChecksum(file);
        System.out.println(hash); //TODO debug
        if (!hashes.contains(hash)) {
            hashes.add(hash);
            userFiles.put(hash, file);
        }
    }

    public ArrayList<String> getHashes() {
        return hashes;
    }
}
