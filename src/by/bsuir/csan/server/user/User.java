package by.bsuir.csan.server.user;

import by.bsuir.csan.helpers.HashHelper;
import by.bsuir.csan.server.Server;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

public class User implements Serializable {

    private String login;
    private String password; //TODO replace with pass hash
    private File userDir;
    private HashMap<File, String> userFilesHashes = new HashMap<>();

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
        if (userFilesHashes.containsKey(new File(filePath))) {
            String serverFilePath = userDir + "/" + filePath;
            return new File(serverFilePath);
        }
        return null;
    }

    public void putFile(File file) {
        String hash = HashHelper.getHash(file);
        String filePath = file.getPath().replaceFirst(userDir.getPath() + "/", "");
        userFilesHashes.put(new File(filePath), hash);
    }

    public HashMap<File, String> getFilesHashes() {
        return userFilesHashes;
    }
}
