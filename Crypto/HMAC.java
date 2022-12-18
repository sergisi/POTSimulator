package Crypto;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
//import org.apache.commons.codec.binary.Base64;

public class HMAC {

	
	public BigInteger ComputeHash(String key, BigInteger value) throws NoSuchAlgorithmException, InvalidKeyException {
		BigInteger result = ComputeHash(key, value.toString());

		return (result);
	}

	
	
	public BigInteger ComputeHash(String key, String value) throws NoSuchAlgorithmException, InvalidKeyException {

		// Si es modifica HmacSHA256 a un altra funci� revisar que els
		// bits que retorna getBitsKey() coincideixi amb la nova HMAC. 
		Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
		SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(), "HmacSHA256");
		sha256_HMAC.init(secret_key);

		byte[] bytes = sha256_HMAC.doFinal(value.getBytes());

		BigInteger result = new BigInteger(bytes);

		return(result);
	}
	
	public int getBitsKey()
	{
		return(256); // Si canvia HmacSHA256 a un altre revisar el retorn.
	}
}