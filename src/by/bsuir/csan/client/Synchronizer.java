package by.bsuir.csan.client;


import by.bsuir.csan.client.settings.ClientSettingsManager;
import by.bsuir.csan.helpers.HashHelper;

import java.io.*;
import java.util.HashMap;

public class Synchronizer {

    private File          userFilesHashesSaveFile = new File("client_hashes.info");
    private ClientSession clientSession;

    public Synchronizer(ClientSession clientSession) {
        this.clientSession = clientSession;
        if (!userFilesHashesSaveFile.exists()) {
            saveFilesHashesToFile(new HashMap<>());
        }
    }

    public void synchronize(HashMap<File, String> serverHashes) {

        HashMap<File, String> clientHashes = getFilesHashesIn(ClientSettingsManager.getRootDir());
        HashMap<File, String> lastClientHashes = getLastFilesHashesFromFile();
        HashMap<File, String> handledServerHashes = new HashMap<>();
        HashMap<File, String> clientHashesToDelete = new HashMap<>();

        for (File clientFile : clientHashes.keySet()) {

            if (serverHashes.containsKey(clientFile)) {

                boolean differentHashesOnClientAndServer =
                        !serverHashes.get(clientFile).equals(clientHashes.get(clientFile));

                if (differentHashesOnClientAndServer) {
                    boolean sameHashesOnClientAndLastClientSync =
                            clientHashes.get(clientFile).equals(lastClientHashes.get(clientFile));

                    if (sameHashesOnClientAndLastClientSync) {
                        clientSession.retrieveFileFromServer(clientFile);
                        clientHashes.put(clientFile, serverHashes.get(clientFile));
                    } else if (ClientSettingsManager.getOverrideOption()) {
                        clientSession.storeFileOnServer(clientFile);
                    } else {
                        clientSession.retrieveFileFromServer(clientFile);
                        clientHashes.put(clientFile, serverHashes.get(clientFile));
                    }
                }
                handledServerHashes.put(clientFile, serverHashes.get(clientFile));
            } else if (lastClientHashes.containsKey(clientFile)) {
                clientHashesToDelete.put(clientFile, clientHashes.get(clientFile));
            } else {
                clientSession.storeFileOnServer(clientFile);
            }
        }

        clientHashesToDelete.keySet().forEach((file) -> {
            clientHashes.remove(file);
            new File(ClientSettingsManager.getRootDir().getPath() + "/" + file.getPath()).delete();
        });

        handledServerHashes.forEach(serverHashes::remove);

        //On server; don't on client side
        for (File serverFile : serverHashes.keySet()) {
            if (lastClientHashes.containsKey(serverFile)) {
                clientSession.deleteFileOnServer(serverFile);
            } else {
                clientSession.retrieveFileFromServer(serverFile);
                clientHashes.put(serverFile, serverHashes.get(serverFile));
            }
        }

        saveFilesHashesToFile(clientHashes);
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

    private HashMap<File, String> getLastFilesHashesFromFile() {
        HashMap<File, String> oldClientsFilesHashes = new HashMap<>();
        try (FileInputStream fin = new FileInputStream(userFilesHashesSaveFile)) {
            ObjectInputStream oin = new ObjectInputStream(fin);

            oldClientsFilesHashes = (HashMap<File, String>) oin.readObject();
            oin.close();
            fin.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return oldClientsFilesHashes;
    }

    private HashMap<File, String> getFilesHashesIn(File directory) {
        HashMap<File, String> userFilesHashes = new HashMap<>();
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                userFilesHashes.putAll(getFilesHashesIn(file));
            } else {
                if (!file.getName().startsWith(".")) {
                    String shortFilePath = file.getPath().replaceFirst(
                            ClientSettingsManager.getRootDir().getPath() + "/", "");
                    userFilesHashes.put(new File(shortFilePath), HashHelper.getHash(file));
                }
            }
        }
        return userFilesHashes;
    }
}
