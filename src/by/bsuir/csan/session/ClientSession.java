package by.bsuir.csan.session;
import by.bsuir.csan.client.Client;
import by.bsuir.csan.helpers.HashHelper;

import java.io.*;
import java.util.HashMap;

public class ClientSession extends Session {

    private File rootDir;
    private boolean overrideOption;
    private File userInfoFile;

    public ClientSession(Client client) throws IOException {
        super(client.getSocket());
        this.rootDir = client.getRootDir();
        this.overrideOption = client.hasOverrideOption();
        this.userInfoFile = new File("users.info");
        saveClientFilesInfo(getFilesIn(rootDir));
    }

    public void saveClientFilesInfo(HashMap<File, String> userFilesInfo) throws IOException {
        FileOutputStream fos = new FileOutputStream(userInfoFile, false);
        ObjectOutputStream out = new ObjectOutputStream(fos);
        out.writeObject(userFilesInfo);
        fos.close();
        out.close();
    }

    private HashMap<File, String> getClientFilesInfo() throws IOException, ClassNotFoundException {
        FileInputStream fin = new FileInputStream(userInfoFile);
        ObjectInputStream oin = new ObjectInputStream(fin);
        HashMap<File, String> oldClientsFiles = (HashMap<File, String>) oin.readObject();
        oin.close();
        fin.close();
        return oldClientsFiles;
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

    public String storeFileOnServer(File file) throws IOException {

        String response = getResponse(STORE_CMD + " " + file.getPath());
        if (response.equals(START_LOADING_MSG)) {
            sendFile(file);
            return receiveMessage();
        }
        return response;
    }

    public String retrieveFileFromServer(File file) throws IOException {
        String response = getResponse(RETR_CMD + " " + file.getPath());
        if (response.equals(START_LOADING_MSG)) {
            receiveFile(file);
            return receiveMessage();
        }
        return response;
    }

    public String quit() throws IOException {
        return getResponse(QUIT_CMD);
    }

    public HashMap<File, String> getFilesIn(File directory) {
        HashMap<File, String> tmpFiles = new HashMap<>();
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                tmpFiles.putAll(getFilesIn(file));
            } else {
                tmpFiles.put(file, HashHelper.getHash(file));
            }
        }
        return tmpFiles;
    }

    @Override
    protected void handleSession() throws IOException {
        try {
            while (true) { //TODO replace with smth correct
                Thread.sleep(10 * 1000); //ten seconds
                String response = getResponse(HASH_CMD);
                if (response.equals(OK_MSG)) {

                    HashMap<File, String> serverHashes = receiveFilesHashes();
                    HashMap<File, String> clientHashes = getFilesIn(rootDir);
                    HashMap<File, String> oldHashes = getClientFilesInfo();
                    System.out.println(serverHashes); //TODO debug
                    System.out.println(clientHashes); //TODO debug
                    System.out.println(oldHashes); //TODO debug
                    System.out.println("==================================="); //TODO debug
                    for (File clientFile : clientHashes.keySet()) {

                        boolean isExistsOnServer = false;

                        for (File serverFile : serverHashes.keySet()) {
                            if (clientFile.equals(serverFile)) {
                                isExistsOnServer = true;

//                                if (!serverHashes.get(serverFile).equals(clientHashes.get(clientFile))) {
//                                    if (clientHashes.get(clientFile).equals(oldHashes.get(clientFile))) {
//                                        loadFileFromServer(clientFile.getPath());
//                                    } else {
//                                        storeFileOnServer(clientFile.getPath());
//                                    }
//                                }
//
                                serverHashes.remove(serverFile);
                            }
                        }

                        System.out.println(serverHashes); //TODO debug
                        System.out.println(clientHashes); //TODO debug
                        System.out.println(oldHashes); //TODO debug

//                        if (!isExistsOnServer) {
//                            if (oldHashes.containsKey(clientFile)) {
//                                clientHashes.remove(clientFile);
//                                clientFile.delete();
//                            } else {
//                                storeFileOnServer(clientFile.getPath());
//                            }
//                        }
//
                    }
//
//                    //Load all files, which are on the server, but don't exist on client side
//                    for (File serverFile : serverHashes.keySet()) {
//                        loadFileFromServer(serverFile.getPath());
//                        clientHashes.put(serverFile, serverHashes.get(serverFile));
//                    }

                    saveClientFilesInfo(clientHashes);
                }
            }
        } catch (InterruptedException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
