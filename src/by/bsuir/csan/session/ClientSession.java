package by.bsuir.csan.session;
import by.bsuir.csan.client.Client;

import java.io.*;

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

    public String quit() throws IOException {
        return getResponse(QUIT_CMD);
    }

    @Override
    protected void handleSession() throws IOException {
        try {
            //getResponse(HASH_CMD);
            //ObjectInputStream ois = new ObjectInputStream(inStream);
            //ArrayList<Integer> serverHashes = (ArrayList<Integer>) ois.readObject();
            Thread.sleep(3*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
