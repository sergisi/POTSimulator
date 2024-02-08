package POT.Entities;

import java.math.BigInteger;

import Crypto.RSA;
import Crypto.RandomGenerator;
import Helpers.HelperString;

public class Item extends POTEntity {

    public static int M = 8;
    public static int MAX_PRICE = (int) (Math.pow(2, M) - 1);
    public static int SIZE_ITEM_KEY = 256;

    public BigInteger value;
    public int price;
    public String priceInBits;
    public BigInteger key;

    public byte[] cypherValue;

    public Item(SimulatorSettings settings, RSA rsa) throws Exception {
        super(settings, rsa);

    }

    public void initialize() throws Exception {
        BigInteger value = super.randomGenerator.generateRandom(255);

        BigInteger maxPrice = new BigInteger(String.valueOf(MAX_PRICE));
        int price = 0;

        while (price <= 0)
            price = super.randomGenerator.generateRandom(settings.SIZE_KEY).mod(maxPrice).intValue() % MAX_PRICE;

        BigInteger key = generateRandomKey();

        this.initialize(value, price, key);
    }

    public void initialize(BigInteger value, int price, BigInteger key) throws Exception {
        this.value = value;
        this.price = price;
        this.key = key;
        this.cypherValue = this.aes.encrypt(value.toString(), key.toString());
        this.priceInBits = Integer.toBinaryString(this.price);
        if (priceInBits.length() < M)
            priceInBits = HelperString.Fill(priceInBits, M, '0');
    }

    public void encryptWithKey(BigInteger key) throws Exception {
        this.cypherValue = this.aes.encrypt(value.toString(), key.toString());
    }

    public static BigInteger generateRandomKey() {
        return new RandomGenerator().generateRandom(SIZE_ITEM_KEY);
    }

    public String toString() {
        return "Item" +
                System.lineSeparator() +
                "  �Value: " + this.value +
                System.lineSeparator() +
                "  �Price: " + this.price +
                System.lineSeparator() +
                "  �Price in bits: " + this.priceInBits +
                System.lineSeparator() +
                "  �Key: " + this.key +
                System.lineSeparator();
    }
}
