package by.bsuir.csan.server.user;

import java.io.File;
import java.io.Serializable;

public class User implements Serializable {

    private String login;
    private String password; //TODO replace with pass hash
    private File userDir;

    public User(String login, String password) {
        this.login = login;
        this.password = password;
        this.userDir = new File(UsersInfo.getUsersRootDir() + "/" + Integer.toString(login.hashCode()));
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

}
