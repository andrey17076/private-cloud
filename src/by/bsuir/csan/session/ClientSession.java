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

    @Override
    protected void handleSession() throws IOException {
        String username = "user";
        String password = "pass";

        log(getResponse(SIGN_CMD + " " + username + " " + password), LogType.FROM);
        log(getResponse(AUTH_CMD + " " + username + " " + password), LogType.FROM);
        //log(getResponse(CHECK_CMD), LogType.FROM);
        log(getResponse(QUIT_CMD), LogType.FROM);
        log("End of session"); //TODO debug
    }
}
