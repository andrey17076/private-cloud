package by.bsuir.csan.server.user;

import by.bsuir.csan.helpers.HashHelper;
import by.bsuir.csan.server.Server;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

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

    public File getFile(String filePath) {
        File tmp_file;
        Iterator<File> iterator = userFiles.values().iterator();
        while (iterator.hasNext()) {
            tmp_file = iterator.next();
            if (tmp_file.getPath().equals(filePath)) {
                return tmp_file;
            }
        }
        return null;
    }

    public void putFile(File file) {

        String contentHash = HashHelper.getChecksum(file);
        String pathHash = Integer.toString(file.hashCode());
        String hash = pathHash + contentHash;

        if (!hashes.contains(hash)) {
            hashes.add(hash);
            userFiles.put(hash, file);
        }
    }

    public ArrayList<String> getHashes() {
        return hashes;
    }
}
