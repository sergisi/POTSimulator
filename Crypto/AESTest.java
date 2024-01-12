package Crypto;

import org.junit.jupiter.api.Test;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.*;

class AESTest {

    @org.junit.jupiter.api.Test
    void encrypt() throws Exception {
        byte[] enc_message = new byte[]{-11, -40, 53, -48, -120, 29, 57, -98, 15, -22, -96, -91, 90, 41, 63, -122, 2, 12, 31, 72, -74, 100, 113, -28, 111, -20, -58, -58, 82, 118, 91, 100, 127, 14, -60, -84, 64, -118, 40, 10, 43, 73, -115, -23, -70, -45, 96, 83, -73, -60, 106, -109, -86, -7, 113, -7, -77, 103, -36, -118, -113, -117, 40, 35, -119, 97, 50, 104, 111, 100, 57, 103, -38, -70, -82, -57, -96, -88, 35, -95};
        String key = "21146971425852281605827730692118564444434280990135802323757114217362";
        String message = "94118003197908184100952528841696037762905288003339973482144392464123566559085";
        var aes = new AES();
        assertArrayEquals(enc_message, aes.encrypt(message, key));
        assertEquals(message, aes.decrypt(enc_message, key));
    }

}