package Crypto;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash {

	
	public BigInteger ComputeHash(BigInteger value) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		BigInteger result = ComputeHash(value.toString());

		return (result);
	}

	public BigInteger ComputeHash(String value) {
		BigInteger result = BigInteger.ONE;

		try {

			MessageDigest hash = MessageDigest.getInstance("SHA-224");

			hash.update(value.getBytes("UTF-8"));

			byte[] digest = hash.digest();
						
			result = new BigInteger(1, digest);

		} catch (Exception ex) {

		}

		return (result);
	}
	
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
}
