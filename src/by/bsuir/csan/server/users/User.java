package by.bsuir.csan.server.users;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

public class User implements Serializable {

    private String    login;
    private String    passHash;
    private File      userDir;
    private UserFiles userFiles;

    User(String login, String passHash, String rootDirPath) {
        this.login = login;
        this.passHash = passHash;
        this.userDir = new File(rootDirPath + "/" + Integer.toString(login.hashCode()));
        this.userFiles = new UserFiles(userDir.getPath());
    }

    String getLogin() {
        return login;
    }

    String getPassHash() {
        return passHash;
    }

    HashMap<File, String> getHashes() {
        return userFiles.getHashes();
    }

    void addFile(File file) {
        userFiles.addFile(file);
    }

    File getFile(String shortFilePath) {
        return userFiles.getFile(shortFilePath);
    }

    void deleteFile(String shortFilePath) {
        userFiles.deleteFile(shortFilePath);
    }

    String getUserDirPath() {
        return userDir.getPath();
    }
}
