

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import Crypto.AES;
import Crypto.ECIES;
import Crypto.Hash;
import Crypto.RSA;
import Crypto.RandomGenerator;
import POT.Entities.ECoin;
import POT.Entities.Item;
import POT.Entities.Server;
import POT.Entities.SimulatorSettings;
import cat.udl.cig.cryptography.signers.Signature;

public class ManagerPOT   {

	
	protected SimulatorSettings settings;
	protected RSA rsa;
	protected ECIES ecies;
	
	protected RandomGenerator randomGenerator;

	protected ECoin valuedECoin, noValuedECoin;
	protected Server server;
	
	protected Date dateTimestampValuedECoin;
	protected Date dateTimestampNoValuedECoin;
	protected Signature signedTimestampValuedEcoin;
	protected Signature signedTimestampNoValuedEcoin;
	
	
	
	public List<Item> items;
	public List<ECoin> valuedECoins, noValuedECoins;

	public ManagerPOT(SimulatorSettings settings, RSA rsa) {

		this.settings = settings;
		this.rsa = rsa;
		this.randomGenerator = new RandomGenerator();
		this.ecies = new ECIES(this.settings.EC);
		
		this.items = new ArrayList<>();

		this.valuedECoins = new ArrayList<>();
		this.noValuedECoins = new ArrayList<>();

	}
	
	public void initialize() throws Exception {
		this.server = new Server(settings, rsa);

		this.valuedECoin = new ECoin(settings, rsa);
		this.valuedECoin.initAsValued();
		this.valuedECoin.S = this.rsa.Sign(this.valuedECoin.M);

		this.noValuedECoin = new ECoin(settings, rsa);
		this.noValuedECoin.initAsNoValued();
		
		this.dateTimestampValuedECoin = new Date();
		this.dateTimestampNoValuedECoin = new Date();
		
		this.signedTimestampValuedEcoin = this.valuedECoin.signTimestamp(this.dateTimestampValuedECoin);
		this.signedTimestampNoValuedEcoin = this.noValuedECoin.signTimestamp(this.dateTimestampNoValuedECoin);

	 

		for (int i = 0; i < this.settings.MAX_TICKETS; i++) {

			Item item = new Item(this.settings, this.rsa);
			item.initialize();

			this.items.add(item);
		}

		for (int i = 0; i < Item.M; i++) {

			ECoin valuedECoin = new ECoin(this.settings, this.rsa);
			valuedECoin.initAsValued();
			valuedECoin.S = this.rsa.Sign(valuedECoin.M);

			this.valuedECoins.add(valuedECoin);
		}

		for (int i = 0; i < Item.M; i++) {

			ECoin noValuedECoin = new ECoin(this.settings, this.rsa);
			noValuedECoin.initAsNoValued();

			this.noValuedECoins.add(noValuedECoin);
		}
	}
	

	public void simulateAsync(int simulations, boolean  executeOT) {

		ArrayList<Integer> ids = new ArrayList<Integer>();

		for (int i = 0; i < simulations - 1; i++)
			ids.add(new Integer(i));

		ids.parallelStream().forEach((id) -> {
			try {

				this.simulate(executeOT);

				// if (id % (this.settings.MAX_POT_SIMULATIONS / 10) == 0)
					System.out.print(". ");

			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		System.out.println(".");

	}

	public void simulateSerial(int simulations, boolean executeOT) throws Exception {

		for (int i = 0; i < simulations ; i++) {
			this.simulate(executeOT);

			// if (simulations> 10 && i % (simulations / 10) == 0)
				System.out.print(". ");
		}

		System.out.print(".");
	}

	public void simulate(boolean executeOT) throws Exception {

		try {

			List<BigInteger> bitKEYS = this.generateBitKEYS(); // K_0, K_1,..,K_M
			List<BigInteger> obliviousTransferKeys = this.generateObliviousTransferKeys(bitKEYS);

			int idxItemToBuy = this.randomGenerator.generateRandom(16).intValue() % this.items.size();
			Item itemToBuy = this.items.get(idxItemToBuy);
			// BigInteger obliviousTransferKey = obliviousTransferKeys.get(idxItemToBuy);
			
			BigInteger obliviousTransferKey = BigInteger.ZERO;
			
			if(executeOT)
			{
				obliviousTransferKey= this.simulateObliviousTransfer(obliviousTransferKeys, idxItemToBuy);
			}
			else 
				obliviousTransferKey= obliviousTransferKeys.get(idxItemToBuy);

			BigInteger itemValue = this.buyItem(itemToBuy, obliviousTransferKey, bitKEYS);
			if (!itemValue.equals(itemToBuy.value))
				throw new Exception("El valor de l'item comprat no correspon a l'original!!!");

		} catch (Exception ex) {

			StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
			for (StackTraceElement stackTraceElement : stackTraceElements)
				System.out.println("StackTrace: " + stackTraceElement.toString());
		}
	}

	// Simulem el protocol de Oblivious Transfer que equival a una signatura cega.
	private BigInteger simulateObliviousTransfer(List<BigInteger> obliviousTransferKeys, int idxItemToBuy)
			throws Exception {

		Hash hash = new Hash();
		AES aes = new AES();

		byte[] itemToBuyEncrypted = null;
		BigInteger hashItem;
		String key;

		// SERVIDOR
		for (int idxItem = 0; idxItem < obliviousTransferKeys.size(); idxItem++) {

			BigInteger obliviousTransferKey = obliviousTransferKeys.get(idxItem);
			hashItem = hash.ComputeHash(String.valueOf(idxItem)).modPow(this.rsa.d, this.rsa.N);
			key = this.rsa.Sign(hashItem).toString();

			byte[] itemEncrypted = aes.Encrypt(obliviousTransferKey.toString(), key);
			if (idxItem == idxItemToBuy)
				itemToBuyEncrypted = itemEncrypted;
		}

		// CLIENT
		hashItem = hash.ComputeHash(String.valueOf(idxItemToBuy)).modPow(this.rsa.d, this.rsa.N);
		BigInteger blindFactor = this.randomGenerator.generateRandom(this.rsa.N);
		BigInteger blindedMessage = this.rsa.BlindMessage(hashItem, blindFactor);

		// SERVIDOR -> Signatura
		BigInteger blindedMessageSigned = this.rsa.Sign(blindedMessage);

		// CLIENT -> Unblind signatura per obtenir la key i desxifrar el contingut.
		key = this.rsa.UnblindSignature(blindedMessageSigned, blindFactor).toString();
		BigInteger result = new BigInteger(aes.Decrypt(itemToBuyEncrypted, key));

		return (result);
	}

	private BigInteger buyItem(Item itemToBuy, BigInteger obliviousTransferKey, List<BigInteger> bitKEYS)
			throws Exception {

		List<BigInteger> buyedKEYS = new ArrayList<>();

		int idxLastValuedECoin = 0;
		int idxLastNoValuedECoin = 0;

		for (int b = 0; b < itemToBuy.priceInBits.length(); b++) {

			if (itemToBuy.priceInBits.charAt(b) == '1') {
				ECoin valuedECoin = this.valuedECoins.get(idxLastValuedECoin);

				BigInteger keyBit = this.spendECoin(valuedECoin, bitKEYS.get(b), true);
				buyedKEYS.add(keyBit);

				idxLastValuedECoin++;
			} else {
				ECoin noValuedECoin = this.noValuedECoins.get(idxLastNoValuedECoin);

				// Es compra la clau amb una moneda sense valor i per tant no se'n fa res.
				BigInteger emptyValue = this.spendECoin(noValuedECoin, bitKEYS.get(b), false);
				buyedKEYS.add(emptyValue);

				idxLastNoValuedECoin++;
			}
		}

		BigInteger itemToBuyKey = this.generateTemporalItemKey(buyedKEYS, obliviousTransferKey, itemToBuy.priceInBits);

		try {

			String decrypedValue = new AES().Decrypt(itemToBuy.cypherValue, itemToBuyKey.toString());

			return (new BigInteger(decrypedValue));

		} catch (Exception ex) {

			System.out.println("Price: " + itemToBuy.price);

			System.out.println("Error decrypting value. Key buyed: " + itemToBuyKey.toString() + " - Key item: "
					+ itemToBuy.key.toString());

			throw ex;
		}
	}

	private BigInteger spendECoin(ECoin eCoin, BigInteger dataToBuy, boolean isECoinValued) throws Exception {

		BigInteger result = BigInteger.ZERO;

		Date timestamp = new Date();

		Signature signature = eCoin.signTimestamp(timestamp);
		ECIES.Result cypherData = this.server.spendECoin(eCoin.M_s, eCoin.M_r, eCoin.S, timestamp, signature,
				dataToBuy);

		if (isECoinValued)
			result = ecies.decypher(cypherData, eCoin.v_r);

		return (result);
	}

	private List<BigInteger> generateObliviousTransferKeys(List<BigInteger> bitKEYS) throws Exception {

		List<BigInteger> result = new ArrayList<>();

		for (int i = 0; i < this.items.size(); i++) {

			Item item = this.items.get(i);
			BigInteger temporalKey = this.generateTemporalItemKey(bitKEYS, item.key, item.priceInBits);

			result.add(temporalKey);
		}

		return (result);
	}

	public BigInteger generateTemporalItemKey(List<BigInteger> bitKEYS, BigInteger initialKey, String bitKeyArray)
			throws Exception {

		BigInteger result = BigInteger.ONE;
		boolean isFirst = true;

		for (int b = 0; b < bitKeyArray.length(); b++) {
			if (bitKeyArray.charAt(b) == '1') {

				if (isFirst) {
					isFirst = false;
					result = initialKey.xor(bitKEYS.get(b));
				} else
					result = result.xor(bitKEYS.get(b));
			}
		}

		return (result);
	}

	private List<BigInteger> generateBitKEYS() {
		List<BigInteger> result = new ArrayList<>();

		for (int i = 0; i < Item.M; i++)
			result.add(Item.generateRandomKey());

		return (result);
	}
}
