package Crypto;

import Helpers.HelperBit;
import cat.udl.cig.ecc.ECPrimeOrderSubgroup;
import cat.udl.cig.ecc.GeneralECPoint;

import java.math.BigInteger;

public class ECIES {
	
	public class Result
	{		
		public Result(byte[] encryptedMessage, GeneralECPoint r)
		{
			this.EncryptedMessage=encryptedMessage;
			this.R = r;
		}
		
		public byte[] EncryptedMessage;
		public GeneralECPoint R;
	}
	
	public ECIES(ECPrimeOrderSubgroup corba)
	{
		this.Corba=corba;
		this.Hash = new Hash();
		this.Random= new RandomGenerator();
		this.BitsKey = HelperBit.GetBits(this.Corba.getSize());
		this.AES = new AES();
	}
	
	protected ECPrimeOrderSubgroup Corba;
	protected Hash Hash;
	protected RandomGenerator Random;
	protected int BitsKey;
	protected AES AES;
	
	public ECIES.Result cypher(BigInteger message, GeneralECPoint publicKey) throws Exception
	{

		BigInteger n = this.Corba.getSize();
		BigInteger r= this.Random.generateRandom(this.BitsKey).mod(n);


		if(r.compareTo(n)>=0)
			throw new Exception("El random escollit �s superior al m�dul");

		GeneralECPoint G = this.Corba.getGenerator();

		GeneralECPoint R = G.pow(r);
		String sKey = publicKey.pow(r).getX().getIntValue().toString();

		String key = this.KDF(sKey);


		byte[] c = this.AES.encrypt(message.toString(), key);

		ECIES.Result result = new ECIES.Result(c, R);

		return(result);
	}


	public BigInteger decypher(ECIES.Result encrypted, BigInteger privateKey) throws Exception
	{
		// S= K_b � R --> K_b = v = Private key
		GeneralECPoint S = encrypted.R.pow(privateKey);
		String sKey = S.getX().getIntValue().toString();
		
		String key = KDF(sKey);
		
		String message = this.AES.decrypt(encrypted.EncryptedMessage, key);
		BigInteger result = new BigInteger(message);
		
		return(result);
	}

	private String KDF(String key)
	{
		String result = this.Hash.ComputeHash(key).toString();
		
		return result;
	}
}
