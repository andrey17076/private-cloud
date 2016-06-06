package by.bsuir.csan.client;
import by.bsuir.csan.client.settings.ClientSettingsManager;
import by.bsuir.csan.sessions.ServerSettings;
import by.bsuir.csan.sessions.Session;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;

class ClientSession extends Session {

    private Synchronizer synchronizer;

    ClientSession() throws IOException {
        super(new Socket(ServerSettings.getServerIP(), ServerSettings.getServerPort()), new File("client.log"));
        this.synchronizer = new Synchronizer(this);
    }

    String storeFileOnServer(File file) {

        String fullPath = ClientSettingsManager.getRootDir().getPath() + "/" + file.getPath();

        String response = getResponse(STORE_CMD + " " + file.getPath());
        if (response.equals(START_LOADING_MSG)) {
            sendFile(new File(fullPath));
            return receiveMessage();
        }
        return response;
    }

    String retrieveFileFromServer(File file) {

        String fullPath = ClientSettingsManager.getRootDir().getPath() + "/" + file.getPath();

        String response = getResponse(RETR_CMD + " " + file.getPath());
        if (response.equals(START_LOADING_MSG)) {
            receiveFile(new File(fullPath));
            return receiveMessage();
        }
        return response;
    }

    String deleteFileOnServer(File file) {
        return getResponse(DEL_CMD + " " + file.getPath());
    }

    String signUp() {
        String login = ClientSettingsManager.getLogin();
        String passHash = ClientSettingsManager.getPassHash();
        return getResponse(SIGN_CMD + " " + login + " " + passHash);
    }

    String authorize() {
        String login = ClientSettingsManager.getLogin();
        String passHash = ClientSettingsManager.getPassHash();
        return getResponse(AUTH_CMD + " " + login + " " + passHash);
    }

    String quit() {
        return getResponse(QUIT_CMD);
    }

    @Override
    protected void handleSessionPermanently() {
        try {
            Thread.sleep(30 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (ClientSettingsManager.getSyncingOption()) {
            String response = getResponse(HASH_CMD);
            if (response.equals(START_LOADING_MSG)) {
                HashMap<File, String> serverHashes = receiveFilesHashes();
                if (receiveMessage().equals(DONE_MSG)) {
                    synchronizer.sync(serverHashes);
                }
            }
        }
    }
}
