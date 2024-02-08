package Crypto;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash {

    private static BigInteger hashToInteger(String hash) {
        int inumber;
        String snumber;
        BigInteger ihash = BigInteger.ZERO;
        for (int i = 0; i < hash.length(); i++) {
            snumber = hash.substring(i, i + 1);
            inumber = Integer.parseInt(snumber, 16);
            ihash = ihash.add(BigInteger.valueOf(inumber)).multiply(BigInteger.valueOf(16));
        }
        return ihash;
    }

    public BigInteger computeHash(BigInteger value) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        return computeHash(value.toString());
    }

    public BigInteger computeHash(String value) {
        try {
            MessageDigest hash = MessageDigest.getInstance("SHA-224");
            hash.update(value.getBytes(StandardCharsets.UTF_8));
            byte[] digest = hash.digest();
            return new BigInteger(1, digest);
        } catch (Exception ignored) {}
        return BigInteger.ONE;
    }
}
