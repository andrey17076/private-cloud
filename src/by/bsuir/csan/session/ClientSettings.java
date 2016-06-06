package by.bsuir.csan.session;

import java.io.*;

public class ClientSettings implements Serializable {

    private static File rootDir = new File(System.getProperty("user.home") + "/PrivateCloud");
    private static final File clientSettingsFile = new File("settings.conf");

    private String login;
    private String passHash;
    private boolean overrideOption = true;
    private boolean syncingOption = false;

    public ClientSettings() throws IOException {
        if (!rootDir.exists()) {
            rootDir.mkdirs();
        }
        saveClientSettings();
    }

    public static ClientSettings getClientSettings() throws IOException, ClassNotFoundException {

        ClientSettings settings;

        if (clientSettingsFile.exists()) {
            FileInputStream fin = new FileInputStream(clientSettingsFile);
            ObjectInputStream oin = new ObjectInputStream(fin);
            settings = (ClientSettings) oin.readObject();
            oin.close();
            fin.close();
        } else {
            settings = new ClientSettings();
        }

        return settings;
    }

    private void saveClientSettings() throws IOException {
        FileOutputStream fos = new FileOutputStream(clientSettingsFile, false);
        ObjectOutputStream out = new ObjectOutputStream(fos);
        out.writeObject(this);
        fos.close();
        out.close();
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) throws IOException {
        this.login = login;
        saveClientSettings();
    }

    public String getPassHash() {
        return passHash;
    }

    public void setPassHash(String passHash) throws IOException {
        this.passHash = passHash;
        saveClientSettings();
    }

    public boolean getSyncingOption() {
        return syncingOption;
    }

    public void setSyncingOption(boolean option) throws IOException {
        syncingOption = option;
        saveClientSettings();
    }

    public boolean getOverrideOption() {
        return overrideOption;
    }

    public void setOverrideOption(boolean option) throws IOException {
        overrideOption = option;
        saveClientSettings();
    }

    public File getRootDir() {
        return rootDir;
    }

    public void setRootDir(File dir) throws IOException {
        rootDir = dir;
        saveClientSettings();
    }
}
