package by.bsuir.csan.client;
import by.bsuir.csan.client.ClientSettings;
import by.bsuir.csan.helpers.HashHelper;
import by.bsuir.csan.sessions.ServerSettings;
import by.bsuir.csan.sessions.Session;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;

public class ClientSession extends Session {

    private File           userFilesHashesSaveFile = new File("user_files.info");
    private ClientSettings clientSettings;

    public ClientSession(ClientSettings settings) throws IOException {
        super(new Socket(ServerSettings.getServerIP(), ServerSettings.getServerPort()), new File("client.log"));
        this.clientSettings = settings;
        saveFilesHashesToFile(new HashMap<>()); //TODO probably something wrong here
    }

    private void saveFilesHashesToFile(HashMap<File, String> userFiles) {
        try (FileOutputStream fos = new FileOutputStream(userFilesHashesSaveFile, false)) {
            ObjectOutputStream out = new ObjectOutputStream(fos);
            out.writeObject(userFiles);
            fos.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private HashMap<File, String> getFilesHashesFromFile() {
        HashMap<File, String> oldClientsFiles = null;
        try (FileInputStream  fin = new FileInputStream(userFilesHashesSaveFile)) {
            ObjectInputStream oin = new ObjectInputStream(fin);

            oldClientsFiles = (HashMap<File, String>) oin.readObject();
            oin.close();
            fin.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return oldClientsFiles;
    }

    private HashMap<File, String> getFilesHashesIn(File directory) {
        HashMap<File, String> userFilesHashes = new HashMap<>();
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                userFilesHashes.putAll(getFilesHashesIn(file));
            } else {
                if (!file.getName().startsWith(".")) {
                    String shortFilePath = file.getPath().replaceFirst(clientSettings.getRootDir().getPath() + "/", "");
                    userFilesHashes.put(new File(shortFilePath), HashHelper.getHash(file));
                }
            }
        }
        return userFilesHashes;
    }


    private String storeFileOnServer(File file) {

        String fullPath = clientSettings.getRootDir().getPath() + "/" + file.getPath();

        String response = getResponse(STORE_CMD + " " + file.getPath());
        if (response.equals(START_LOADING_MSG)) {
            sendFile(new File(fullPath));
            return receiveMessage();
        }
        return response;
    }

    private String retrieveFileFromServer(File file) {

        String fullPath = clientSettings.getRootDir().getPath() + "/" + file.getPath();

        String response = getResponse(RETR_CMD + " " + file.getPath());
        if (response.equals(START_LOADING_MSG)) {
            receiveFile(new File(fullPath));
            return receiveMessage();
        }
        return response;
    }

    private String deleteFileOnServer(File file) {
        return getResponse(DEL_CMD + " " + file.getPath());
    }

    public void setClientSettings(ClientSettings clientSettings) {
        this.clientSettings = clientSettings;
    }

    public String signUp(String username, String passHash) throws IOException {
        return getResponse(SIGN_CMD + " " + username + " " + passHash);
    }

    public String authorize(ClientSettings clientSettings) throws IOException {
        String response = getResponse(AUTH_CMD + " " + clientSettings.getLogin() + " " + clientSettings.getPassHash());
        if (response.equals(OK_MSG)) {
            this.clientSettings = clientSettings;
        }
        return response;
    }

    public String quit() throws IOException {
        return getResponse(QUIT_CMD);
    }

    @Override
    protected void handleSessionPermanently() {
        try {
            Thread.sleep(30 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (clientSettings != null && clientSettings.getSyncingOption()) {
            String response = getResponse(HASH_CMD);
            if (response.equals(OK_MSG)) {

                HashMap<File, String> serverFiles = receiveFilesHashes();
                HashMap<File, String> clientFiles = getFilesHashesIn(clientSettings.getRootDir());
                HashMap<File, String> oldFiles = getFilesHashesFromFile();
                HashMap<File, String> clientFilesToDelete = new HashMap<>();

                System.out.println("BEFORE============================="); //TODO debug
                System.out.println("Server hashes " + serverFiles);
                System.out.println("Client hashes " + clientFiles);
                System.out.println("Old    hashes " + oldFiles);
                System.out.println("===================================");

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
                            } else if (clientSettings.getOverrideOption()) {
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
                System.out.println("Rest of   Server hashes " + serverFiles);
                System.out.println("To del on client hashes " + clientFilesToDelete);

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
                    new File(clientSettings.getRootDir() + "/" + file.getPath()).delete();
                });

                System.out.println("AFTER=============================="); //TODO debug
                System.out.println("Server hashes " + serverFiles);
                System.out.println("Client hashes " + clientFiles);

                saveFilesHashesToFile(clientFiles);
            }

        }
    }
}