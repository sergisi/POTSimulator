package POT.Entities;

import Crypto.AES;
import Crypto.ECIES;
import Crypto.Hash;
import Crypto.OAEP;
import Crypto.RSA;
import Crypto.RandomGenerator;
import cat.udl.cig.ecc.GeneralECPoint;

public abstract class POTEntity {
	
	protected SimulatorSettings settings;
	protected OAEP oaep;
	protected RandomGenerator randomGenerator;
	protected GeneralECPoint generatorEC;

	protected RSA rsa;
	
	protected Hash hash;
	protected ECIES ecies;
	protected AES aes;

	public POTEntity(SimulatorSettings settings, RSA rsa) throws Exception   {
		
		this.settings = settings;
		this.randomGenerator = new RandomGenerator();
		this.generatorEC = settings.EC.getGenerator();
		this.rsa = rsa;

		this.hash = new Hash();
		this.aes = new AES();
		
		this.ecies = new ECIES(settings.EC);
		
		this.oaep = new OAEP();		
		this.oaep.Initialize(settings.SIZE_POINT + 8, settings.SIZE_KEY - settings.SIZE_POINT - 8, 0,
				settings.SIZE_KEY);
	}
}
