package by.bsuir.csan.session;

import by.bsuir.csan.server.Server;
import by.bsuir.csan.server.user.User;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class ServerSession extends Session {

    private final static String HANDLER_HEAD = "handle";

    private String username = null;
    private String password = null;

    private boolean isAuthorized() throws IOException {
        if ((username == null) || (password == null)) {
            sendMessage(NOT_AUTHORIZED_MSG);
            return false;
            //throw new SessionException("User not authorized");
        }

        return true;
    }

    @Override
    protected void handleSession() throws IOException {

        String textMessage;

        while ((textMessage = receiveMessage()) != null) {

            StringTokenizer messageTokens = new StringTokenizer(textMessage);
            String command = messageTokens.nextToken();
            log(command, LogType.FROM);

            if (command.equals(QUIT_CMD)) {
                sendMessage(OK_MSG);
                log(DISCONNECT_MSG, LogType.FROM);
                socket.close();
                return;
            }

            try {
                Method handleMethod = getClass().getMethod(HANDLER_HEAD + command, StringTokenizer.class);
                handleMethod.invoke(this, messageTokens);
            } catch (NoSuchMethodException e) {
                sendMessage(COMMAND_MISSING_MSG);
            } catch ( IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    public ServerSession(Socket socket) throws IOException {
        super(socket);
        log(CONNECT_MSG, LogType.FROM);
    }

    public void handleSIGN(StringTokenizer messageTokens) throws IOException {

        username = messageTokens.nextToken();
        password = messageTokens.nextToken();

        ArrayList<User> users = Server.getUsers();

        boolean isExists = false;

        for (User user : users) {
            if (user.getLogin().equals(username)) {
                isExists = true;
                break;
            }
        }

        if (isExists) {
            sendMessage(USER_EXISTS_MSG);
        } else {
            Server.putUser(new User(username, password));
            sendMessage(OK_MSG);
        }
    }

    public void handleAUTH(StringTokenizer messageTokens) throws IOException {
        username = messageTokens.nextToken();
        password = messageTokens.nextToken();

        if (isAuthorized()) {
            sendMessage(OK_MSG);
        }
    }

    public void handleCHECK(StringTokenizer messageTokes) throws IOException {
        if (isAuthorized()) {
            sendMessage(OK_MSG);
        }
    }
}
