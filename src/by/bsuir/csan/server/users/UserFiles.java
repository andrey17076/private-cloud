package by.bsuir.csan.server.users;

import by.bsuir.csan.helpers.HashHelper;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

class UserFiles implements Serializable {

    private HashMap<File, String> userFilesHashes;
    private String                userDirPath;

    UserFiles(String userDirPath) {
        this.userFilesHashes = new HashMap<>();
        this.userDirPath = userDirPath;
        new File(userDirPath).mkdir();
    }

    HashMap<File, String> getHashes() {
        return userFilesHashes;
    }

    void addFile(File file) {
        String hash = HashHelper.getHash(file);
        String shortFilePath = file.getPath().replaceFirst(userDirPath + "/", "");
        userFilesHashes.put(new File(shortFilePath), hash);
    }

    File getFile(String shortFilePath) {
        if (userFilesHashes.containsKey(new File(shortFilePath))) {
            String fullFilePath = userDirPath + "/" + shortFilePath;
            return new File(fullFilePath);
        }
        return null;
    }

    void deleteFile(String shortFilePath) {
        String fullFilePath = userDirPath + "/" + shortFilePath;
        new File(fullFilePath).delete();
        userFilesHashes.remove(new File(shortFilePath));
    }
}
