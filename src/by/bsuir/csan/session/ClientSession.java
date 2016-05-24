package by.bsuir.csan.session;
import by.bsuir.csan.client.Client;

import java.io.*;
import java.util.ArrayList;

public class ClientSession extends Session {

    private File rootDir;
    private boolean overrideOption;

    public ClientSession(Client client) throws IOException {
        super(client.getSocket());
        this.rootDir = client.getRootDir();
        this.overrideOption = client.hasOverrideOption();
    }

    public String signUp(String username, String password) throws IOException {
        return getResponse(SIGN_CMD + " " + username + " " + password);
    }

    public String authorize(String username, String password) throws IOException {
        return getResponse(AUTH_CMD + " " + username + " " + password);
    }

    public String checkAuthorization() throws IOException {
        return getResponse(CHECK_CMD);
    }

    public String storeFileOnServer(String filePath) throws IOException {

        sendMessage(STORE_CMD + " " + filePath);

        File file = new File(rootDir + "/" + filePath);
        sendFile(file);

        return receiveMessage();
    }

    public String loadFileFromServer(String filePath) throws IOException {
        sendMessage(LOAD_CMD + " " + filePath);
        String response = receiveMessage();
        if (response.equals(OK_MSG)) {
            receiveFile(rootDir + "/" + filePath);
        }
        return response;
    }

    public String quit() throws IOException {
        return getResponse(QUIT_CMD);
    }


    @Override
    protected void handleSession() throws IOException {
        try {
            while (true) { //TODO replace with smth correct
                sendMessage(HASH_CMD);
                ObjectInputStream ois = new ObjectInputStream(dataInputStream);
                ArrayList<String> serverHashes = (ArrayList<String>) ois.readObject();
                receiveMessage();
                serverHashes.forEach(System.out::println); //TODO debug
                Thread.sleep(5 * 1000); //five seconds
            }
        } catch (InterruptedException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
