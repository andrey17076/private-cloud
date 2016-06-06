package by.bsuir.csan.client.settings;

import java.io.*;

class ClientSettings implements Serializable {

    private String  login;
    private String  passHash;
    private boolean overrideOption = true;
    private boolean syncingOption = false;
    private File    rootDir = new File(System.getProperty("user.home") + "/PrivateCloud");

    String getLogin() {
        return login;
    }

    String getPassHash() {
        return passHash;
    }

    void setLoginInfo(String login, String passHash) {
        this.login = login;
        this.passHash = passHash;
    }

    boolean getSyncingOption() {
        return syncingOption;
    }

    void setSyncingOption(boolean option) {
        syncingOption = option;
    }

    boolean getOverrideOption() {
        return overrideOption;
    }

    void setOverrideOption(boolean option) {
        overrideOption = option;
    }

    File getRootDir() {
        return rootDir;
    }

    void setRootDir(File dir) {
        rootDir = dir;
    }
}
