/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cat.udl.cig.cryptography.hashes;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Models a wrapper over the SHA1 hash algorithm implemented in the Java SDK.
 * 
 * @author Víctor Mateu
 * @author M.Àngels Cerveró
 */
// Base code by Víctor Mateu; Refactorization by M.Àngels Cerveró.
public class SHA1 {
  
    public static String getHash(String message) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            return String.format("%040x",
                    new BigInteger(1, md.digest(message.getBytes())));
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }   
    } 
    
    
    public static BigInteger computetHash(String message, String algorithm) {
        try {
            
        	MessageDigest md = MessageDigest.getInstance(algorithm);
            return new BigInteger(1, md.digest(message.getBytes()));
            
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }   
    }
    
    
    
    public static BigInteger hashToInteger(String hash) {
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
