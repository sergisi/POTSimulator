package Crypto;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.SecureRandom;

public class AES {

	
	public byte[] Encrypt(String message, String key) throws Exception {
	 
	      SecureRandom sr = new SecureRandom(key.getBytes());
	      KeyGenerator kg = KeyGenerator.getInstance("AES");
	      kg.init(sr);
	      SecretKey sk = kg.generateKey();
	  
	      Cipher cipher = Cipher.getInstance("AES");
	      cipher.init(Cipher.ENCRYPT_MODE, sk);
	  
	      byte[] result = cipher.doFinal(message.getBytes());
	  
	      return (result);
	   }
	
	 
	
	
	  
	   public String Decrypt(byte[] messageEncrypted, String key) throws Exception {

		   SecureRandom sr = new SecureRandom(key.getBytes());
		   KeyGenerator kg = KeyGenerator.getInstance("AES");
		   kg.init(sr);
		   SecretKey sk = kg.generateKey();
	  
	      // do the decryption with that key
	      Cipher cipher = Cipher.getInstance("AES");
	      cipher.init(Cipher.DECRYPT_MODE, sk);
	      
	      byte[] decrypted = cipher.doFinal(messageEncrypted);
	      
	      String result = new String(decrypted); 
	  
	      return (result);
	   }
	
}
