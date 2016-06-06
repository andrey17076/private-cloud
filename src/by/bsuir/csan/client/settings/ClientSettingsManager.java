package by.bsuir.csan.client.settings;

import java.io.*;

public class ClientSettingsManager {

    private static final File clientSettingsFile = new File("settings.conf");
    private static ClientSettings clientSettings = new ClientSettings();

    public static void loadSettingsFromFile() {

        if (!clientSettingsFile.exists()) {
            saveSettingsToFile();
        }

        try (FileInputStream fin = new FileInputStream(clientSettingsFile)) {
            ObjectInputStream oin = new ObjectInputStream(fin);
            clientSettings = (ClientSettings) oin.readObject();
            oin.close();
            fin.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void saveSettingsToFile() {
        try (FileOutputStream fos = new FileOutputStream(clientSettingsFile, false)) {
            ObjectOutputStream out = new ObjectOutputStream(fos);
            out.writeObject(clientSettings);
            fos.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getLogin() {
        return clientSettings.getLogin();
    }

    public static String getPassHash() {
        return clientSettings.getPassHash();
    }

    public static void setLoginInfo(String login, String passHash) {
        clientSettings.setLoginInfo(login, passHash);
        saveSettingsToFile();
    }

    public static boolean getSyncingOption() {
        return clientSettings.getSyncingOption();
    }

    public static void setSyncingOption(boolean option) {
        clientSettings.setSyncingOption(option);
        saveSettingsToFile();
    }

    public static boolean getOverrideOption() {
        return clientSettings.getOverrideOption();
    }

    public static void setOverrideOption(boolean option) {
        clientSettings.setOverrideOption(option);
        saveSettingsToFile();
    }

    public static File getRootDir() {
        return clientSettings.getRootDir();
    }

    public static void setRootDir(File dir) {
        clientSettings.setRootDir(dir);
        saveSettingsToFile();
    }
}
