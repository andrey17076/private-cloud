package by.bsuir.csan.session;
import by.bsuir.csan.helpers.HashHelper;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;

public class ClientSession extends Session {

    private static Socket socket;

    static {
        try {
            socket = new Socket(ServerSettings.getServerIP(), ServerSettings.getServerPort());
            socket.setSoTimeout(15);
        } catch (IOException e) {
            //null instead of ClientSession object
        }
    }

    private File userFilesInfoFile = new File("user_files.info");
    private ClientSettings clientSettings;
    private boolean isConnected = true;

    public ClientSession() throws IOException {
        super(socket, new File("client.log"));
        saveClientFilesInfo(new HashMap<>()); //TODO probably something wrong here
    }

    private void saveClientFilesInfo(HashMap<File, String> userFiles) throws IOException {
        FileOutputStream fos = new FileOutputStream(userFilesInfoFile, false);
        ObjectOutputStream out = new ObjectOutputStream(fos);
        out.writeObject(userFiles);
        fos.close();
        out.close();
    }

    private HashMap<File, String> getClientFilesInfo() throws IOException, ClassNotFoundException {
        FileInputStream fin = new FileInputStream(userFilesInfoFile);
        ObjectInputStream oin = new ObjectInputStream(fin);
        HashMap<File, String> oldClientsFiles = (HashMap<File, String>) oin.readObject();
        oin.close();
        fin.close();
        return oldClientsFiles;
    }

    private HashMap<File, String> getFilesIn(File directory) {
        HashMap<File, String> userFiles = new HashMap<>();
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                userFiles.putAll(getFilesIn(file));
            } else {
                userFiles.put(file, HashHelper.getHash(file));
            }
        }
        return userFiles;
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

    public String checkAutorization() throws IOException {
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
        isConnected = false;
        return getResponse(QUIT_CMD);
    }

    @Override
    protected void handleSession() throws IOException {
        try {
            while (isConnected) {
                Thread.sleep(10 * 1000);
                if (clientSettings.getSyncingOption()) {

                    String response = getResponse(HASH_CMD);
                    if (response.equals(OK_MSG)) {

                        HashMap<File, String> serverFiles = receiveFilesHashes();
                        HashMap<File, String> clientFiles = getFilesIn(clientSettings.getRootDir());
                        HashMap<File, String> oldFiles = getClientFilesInfo();
                        HashMap<File, String> clientFilesToDelete = new HashMap<>();

                        System.out.println("BEFORE============================="); //TODO debug
                        System.out.println("Server hashes " + serverFiles); //TODO debug
                        System.out.println("ClientSettings hashes " + clientFiles); //TODO debug
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
                        System.out.println("ClientSettings hashes " + clientFiles); //TODO debug

                        saveClientFilesInfo(clientFiles);
                    }

                }
            }
        } catch (InterruptedException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
