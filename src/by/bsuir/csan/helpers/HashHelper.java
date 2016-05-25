package by.bsuir.csan.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashHelper {

    private static byte[] createChecksum(File file) throws IOException, NoSuchAlgorithmException {
        FileInputStream fis =  new FileInputStream(file);
        byte[] buffer = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance("MD5");
        int numRead;
        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);

        fis.close();
        return complete.digest();
    }

    public static String getHash(File file) {

        byte[] hashBytes = new byte[0];

        try {
            hashBytes = createChecksum(file);
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        String result = "";

        for (byte hashByte : hashBytes) {
            result += Integer.toString((hashByte & 0xff) + 0x100, 16).substring(1);
        }

        return result;
    }
}