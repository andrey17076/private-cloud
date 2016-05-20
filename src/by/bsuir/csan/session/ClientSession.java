package by.bsuir.csan.session;
import by.bsuir.csan.client.Client;

import java.io.File;
import java.io.IOException;

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

    public String authenticate(String username, String password) throws IOException {
        return getResponse(AUTH_CMD + " " + username + " " + password);
    }

    public String checkAuthentification() throws IOException {
        return getResponse(CHECK_CMD);
    }

    public String quit() throws IOException {
        return getResponse(QUIT_CMD);
    }

    @Override
    protected void handleSession() throws IOException {

        String username = "user";
        String password = "pass";

        signUp(username, password);
        quit();

        log("End of session"); //TODO debug
    }
}
