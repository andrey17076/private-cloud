package by.bsuir.csan.server;

import by.bsuir.csan.helpers.RegExpHelper;
import by.bsuir.csan.server.users.User;
import by.bsuir.csan.server.users.UsersManager;
import by.bsuir.csan.sessions.Session;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.ArrayList;

public class ServerSession extends Session {

    private static final String HANDLER_HEAD = "handle";
    private static final String WORD_REGEX = "\\S+";
    private User                user;

    public ServerSession(Socket clientSocket, File logFile) throws IOException {
        super(clientSocket, logFile);
        new Thread(this).start(); //start handling this sessions: receiving messages from client
    }

    @Override
    protected void handleSessionPermanently() {
        String            message = receiveMessage();
        ArrayList<String> messageWords = RegExpHelper.getMatches(message, WORD_REGEX);
        String            command = messageWords.get(0);
        String            appendedArgsLine = message.replaceFirst(command + "\\s", "");
        performCommand(command, appendedArgsLine);
    }

    private void performCommand(String command, String argsLine) {
        try {
            Method handleMethod = getClass().getDeclaredMethod(HANDLER_HEAD + command, String.class);
            handleMethod.setAccessible(true);
            handleMethod.invoke(this, argsLine);
        } catch (NoSuchMethodException e) {
            sendMessage(COMMAND_MISSING_MSG);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private boolean isAuthorized() {
        if (user == null) {
            sendMessage(NOT_AUTHORIZED_MSG);
            return false;
        }
        return true;
    }

    private void handleSIGN(String argsLine) {
        ArrayList<String> args = RegExpHelper.getMatches(argsLine, WORD_REGEX);
        String            login = args.get(0);
        String            passHash = args.get(1);

        if (UsersManager.isUserExists(login)) {
            sendMessage(USER_EXISTS_MSG);
        } else {
            UsersManager.addUser(login, passHash);
            sendMessage(OK_MSG);
        }
    }

    private void handleAUTH(String argsLine) {
        ArrayList<String> args = RegExpHelper.getMatches(argsLine, WORD_REGEX);
        String            login = args.get(0);
        String            passHash = args.get(1);
        User              user = UsersManager.getUser(login, passHash);

        if (user == null) {
            sendMessage(USER_NOT_EXIST_MSG);
        } else {
            this.user = user;
            sendMessage(OK_MSG);
        }
    }

    private void handleHASH(String argsLine) {
        if (isAuthorized()) {
            sendMessage(OK_MSG);
            sendFilesHashes(UsersManager.getUserHashes(user));
        } else {
            sendMessage(NOT_AUTHORIZED_MSG);
        }
    }

    private void handleSTORE(String shortFilePath) {
        if (isAuthorized()) {
            sendMessage(START_LOADING_MSG);
            File file = receiveFile(new File(UsersManager.getUserDirPath(user) + "/" +  shortFilePath));
            UsersManager.addFileTo(user, file);
            sendMessage(OK_MSG);
        } else {
            sendMessage(NOT_AUTHORIZED_MSG);
        }
    }

    private void handleRETR(String shortFilePath) {
        if (isAuthorized()) {
            File file = UsersManager.getFileFrom(user, shortFilePath);
            sendMessage(START_LOADING_MSG);
            sendFile(file);
            sendMessage(OK_MSG);
        } else {
            sendMessage(NOT_AUTHORIZED_MSG);
        }
    }

    private void handleDEL(String shortFilePath) {
        if (isAuthorized()) {
            sendMessage(OK_MSG);
            UsersManager.deleteFileFrom(user, shortFilePath);
        } else {
            sendMessage(NOT_AUTHORIZED_MSG);
        }
    }

    private void handleQUIT(String argLine) {
        user = null;
        sendMessage(OK_MSG);
    }
}
