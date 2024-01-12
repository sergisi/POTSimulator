package Crypto;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

public class AES {
    public static final String SALT = "Some Salt Value";

    public byte[] encrypt(String message, String key) throws Exception {
        var sk = generateSecretKey(key);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, sk);
        byte[] bytes = cipher.doFinal(message.getBytes());
        return bytes;
    }

    private static SecretKey generateSecretKey(String key) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(key.toCharArray(), SALT.getBytes(), 65536, 256);
        return new SecretKeySpec(factory.generateSecret(spec)
                .getEncoded(), "AES");
    }


    public String decrypt(byte[] messageEncrypted, String key) throws Exception {
        var sk = generateSecretKey(key);
        // do the decryption with that key
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, sk);

        byte[] decrypted = cipher.doFinal(messageEncrypted);

        return new String(decrypted);
    }

}
