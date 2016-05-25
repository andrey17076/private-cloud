package by.bsuir.csan.session;
import by.bsuir.csan.client.Client;
import by.bsuir.csan.helpers.HashHelper;

import java.io.*;
import java.util.HashMap;

public class ClientSession extends Session {

    private File rootDir;
    private boolean overrideOptionChoosed;
    private File userInfoFile;

    public ClientSession(Client client) throws IOException {
        super(client.getSocket());
        this.rootDir = client.getRootDir();
        this.overrideOptionChoosed = client.hasOverrideOption();
        this.userInfoFile = new File("user.info");
        saveClientFilesInfo(new HashMap<>());
    }

    private void saveClientFilesInfo(HashMap<File, String> userFilesInfo) throws IOException {
        FileOutputStream fos = new FileOutputStream(userInfoFile, false);
        ObjectOutputStream out = new ObjectOutputStream(fos);
        out.writeObject(userFilesInfo);
        fos.close();
        out.close();
    }

    private HashMap<File, String> getFilesIn(File directory) {
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
        if (response.equals(OK_MSG)) {
            receiveFile(file);
        }
        return response;
    }

    public String deleteFileOnServer(File file) throws IOException {
        return getResponse(DEL_CMD + " " + file.getPath());
    }

    public String quit() throws IOException {
        return getResponse(QUIT_CMD);
    }

    @Override
    protected void handleSession() throws IOException {
        try {
            while (true) { //TODO replace with smth correct
                Thread.sleep(10 * 1000);
                String response = getResponse(HASH_CMD);
                if (response.equals(OK_MSG)) {

                    HashMap<File, String> serverFiles = receiveFilesHashes();
                    HashMap<File, String> clientFiles = getFilesIn(rootDir);
                    HashMap<File, String> oldFiles = getClientFilesInfo();
                    HashMap<File, String> clientFilesToDelete = new HashMap<>();

                    System.out.println("BEFORE============================="); //TODO debug
                    System.out.println("Server hashes " + serverFiles); //TODO debug
                    System.out.println("Client hashes " + clientFiles); //TODO debug
                    System.out.println("Old    hashes " + oldFiles); //TODO debug
                    System.out.println("==================================="); //TODO debug

                    for (File clientFile : clientFiles.keySet()) {

                        if (serverFiles.containsKey(clientFile)) {

                            boolean differentHashesOnClientAndServer =
                                    !serverFiles.get(clientFile).equals(clientFiles.get(clientFile));

                            if (differentHashesOnClientAndServer) {
                                boolean sameHashesOnClientAndLastClientSync =
                                        clientFiles.get(clientFile).equals(oldFiles.get(clientFile));

                                if (sameHashesOnClientAndLastClientSync) {
                                    retrieveFileFromServer(clientFile);
                                    clientFiles.put(clientFile, serverFiles.get(clientFile));
                                } else if (overrideOptionChoosed) {
                                    storeFileOnServer(clientFile);
                                } else {
                                    retrieveFileFromServer(clientFile);
                                    clientFiles.put(clientFile, serverFiles.get(clientFile));
                                }
                            }
                            serverFiles.remove(clientFile);
                        } else if (oldFiles.containsKey(clientFile)) {
                            clientFilesToDelete.put(clientFile, clientFiles.get(clientFile));
                        } else {
                            storeFileOnServer(clientFile);
                        }
                    }

                    System.out.println("PRE================================"); //TODO debug
                    System.out.println("Server hashes " + serverFiles); //TODO debug
                    System.out.println("Delete hashes " + clientFilesToDelete); //TODO debug

                    for (File serverFile : serverFiles.keySet()) { //On the server; Don't on the client side
                        if (oldFiles.containsKey(serverFile)) {
                            deleteFileOnServer(serverFile);
                        } else {
                            retrieveFileFromServer(serverFile);
                            clientFiles.put(serverFile, serverFiles.get(serverFile));
                        }
                    }

                    clientFilesToDelete.keySet().forEach((file) -> {
                        clientFiles.remove(file);
                        file.delete();
                    });

                    System.out.println("AFTER=============================="); //TODO debug
                    System.out.println("Server hashes " + serverFiles); //TODO debug
                    System.out.println("Client hashes " + clientFiles); //TODO debug

                    saveClientFilesInfo(clientFiles);
                }

            }
        } catch (InterruptedException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
