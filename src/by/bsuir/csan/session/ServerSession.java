package by.bsuir.csan.session;

import by.bsuir.csan.server.user.User;
import by.bsuir.csan.server.user.UsersInfo;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.StringTokenizer;

public class ServerSession extends Session {

    private final static String HANDLER_HEAD = "handle";

    private User user = null;

    private boolean isAuthorized() throws IOException {

        if (user == null) {
            sendMessage(NOT_AUTHORIZED_MSG);
            return false;
        }

        return true;
    }

    @Override
    protected void handleSession() {

        String textMessage;

        try {
            while ((textMessage = receiveMessage()) != null) {

                StringTokenizer messageTokens = new StringTokenizer(textMessage);
                String command = messageTokens.nextToken();
                try {
                    Method handleMethod = getClass().getMethod(HANDLER_HEAD + command, StringTokenizer.class);
                    handleMethod.invoke(this, messageTokens);
                } catch (NoSuchMethodException e) {
                    sendMessage(COMMAND_MISSING_MSG);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) { //in case when socket was closed
            log(DISCONNECT_MSG, LogType.FROM);
        }
    }

    public ServerSession(Socket socket) throws IOException {
        super(socket);
        log(CONNECT_MSG, LogType.FROM);
    }

    public void handleSIGN(StringTokenizer messageTokens) throws IOException {

        String username = messageTokens.nextToken();
        String password = messageTokens.nextToken();

        boolean isExists = false;

        for (User u : UsersInfo.getUsers()) {
            if (u.getLogin().equals(username)) {
                isExists = true;
                break;
            }
        }

        if (isExists) {
            sendMessage(USER_EXISTS_MSG);
        } else {
            UsersInfo.addUser(new User(username, password));
            sendMessage(OK_MSG);
        }
    }

    public void handleAUTH(StringTokenizer messageTokens) throws IOException {

        String username = messageTokens.nextToken();
        String password = messageTokens.nextToken();

        boolean isSignedUp = false;

        for (User u : UsersInfo.getUsers()) {
            if (u.getLogin().equals(username)) {
                if (u.getPassword().equals(password)) {
                    isSignedUp = true;
                    this.user = u;
                } else {
                    sendMessage(WRONG_PASSWORD_MSG);
                    return;
                }
                break;
            }
        }

        if (isSignedUp) {
            sendMessage(OK_MSG);
        } else {
            sendMessage(USER_NOT_EXISTS_MSG);
        }
    }

    public void handleHASH(StringTokenizer messageTokens) throws IOException {
        if (isAuthorized()) {
            sendMessage(START_LOADING_MSG);
            sendFilesHashes(UsersInfo.getUserInfo(user));
            sendMessage(OK_MSG);
        } else {
            sendMessage(NOT_AUTHORIZED_MSG);
        }
    }

    public void handleSTORE(StringTokenizer messageTokens) throws IOException {

        if (isAuthorized()) {
            sendMessage(START_LOADING_MSG);
            String filePath = messageTokens.nextToken();
            File file = receiveFile(new File(user.getUserDir().getPath() + "/" + filePath));
            UsersInfo.addFileTo(user, file);
            sendMessage(OK_MSG);
        } else {
            sendMessage(NOT_AUTHORIZED_MSG);
        }
    }

    public void handleRETR(StringTokenizer messageTokens) throws IOException {
        if (isAuthorized()) {
            File file = UsersInfo.getFileFrom(user, messageTokens.nextToken());
            if (file != null) {
                sendMessage(START_LOADING_MSG);
                sendFile(file);
                sendMessage(OK_MSG);
            } else {
                sendMessage(NOT_FOUND_MSG);
            }
        } else {
            sendMessage(NOT_AUTHORIZED_MSG);
        }
    }

    public void handleCHECK(StringTokenizer messageTokens) throws IOException {
        if (isAuthorized()) {
            sendMessage(OK_MSG);
        }
    }

    public void handleQUIT(StringTokenizer messageTokens) throws IOException {
        sendMessage(OK_MSG);
        socket.close();
    }
}
