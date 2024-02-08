import Crypto.*;
import POT.Entities.*;
import cat.udl.cig.cryptography.signers.Signature;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ManagerPOTImproved {
    public List<Item> items;
    public List<ECoin> valuedECoins, noValuedECoins;
    protected SimulatorSettings settings;
    protected RSA rsa;
    protected DSA dsa;
    protected ECIES ecies;
    protected RandomGenerator randomGenerator;
    protected Server server;
    /**
     * The server ones for bitKeys and Oblivious Transfer are stored in the class. The client Recomputes them in the
     * methods.
     */
    List<BigInteger> bitKEYS; // K_0, K_1,..,K_M
    ArrayList<ByteArrayBox> encryptedOTItems;
    List<BigInteger> obliviousTransferKeys;

    public ManagerPOTImproved(SimulatorSettings settings, RSA rsa, DSA dsa) {
        this.settings = settings;
        this.rsa = rsa;
        this.dsa = dsa;
        this.randomGenerator = new RandomGenerator();
        this.ecies = new ECIES(this.settings.EC);
        this.items = new ArrayList<>();
        this.valuedECoins = new ArrayList<>();
        this.noValuedECoins = new ArrayList<>();
        encryptedOTItems = new ArrayList<>();
    }

    public void initialize() throws Exception {
        this.server = new Server(settings, rsa);
        for (int i = 0; i < this.settings.MAX_TICKETS; i++) {
            Item item = new Item(this.settings, this.rsa);
            item.initialize();
            item.key = dsa.g.modPow(item.key.mod(dsa.q), dsa.N);
            if (!item.key.modPow(dsa.q, dsa.N).equals(BigInteger.ONE)) {
                throw new Exception("Couldn't generate the key for the group.");
            }

            this.items.add(item);
        }
        for (int i = 0; i < Item.M; i++) {
            ECoin valuedECoin = new ECoin(this.settings, this.rsa);
            valuedECoin.initAsValued();
            valuedECoin.S = this.rsa.sign(valuedECoin.M);
            this.valuedECoins.add(valuedECoin);
            ECoin noValuedECoin = new ECoin(this.settings, this.rsa);
            noValuedECoin.initAsNoValued();
            this.noValuedECoins.add(noValuedECoin);
        }
        bitKEYS = this.generateBitKEYS();
        obliviousTransferKeys = this.generateObliviousTransferKeys();
        for (int i = 0; i < items.size(); i++) {
            items.get(i).encryptWithKey(obliviousTransferKeys.get(i));
        }
        generateOTEncryptedItemList();
    }

    private void generateOTEncryptedItemList() throws Exception {
        Hash hash = new Hash();
        AES aes = new AES();
        BigInteger hashItem;
        String key;
        for (int idxItem = 0; idxItem < obliviousTransferKeys.size(); idxItem++) {
            BigInteger itemKey = items.get(idxItem).key;
            hashItem = hash.computeHash(String.valueOf(idxItem)).modPow(this.rsa.d, this.rsa.N);
            key = this.rsa.sign(hashItem).toString();
            byte[] oTitemEncrypted = aes.encrypt(itemKey.toString(), key);
            encryptedOTItems.add(new ByteArrayBox(oTitemEncrypted));
        }
    }


    public void simulateAsync(int simulations) throws RuntimeException {
        IntStream.range(0, simulations).parallel().forEach((id) -> {
            try {
                this.simulate();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            System.out.print(". ");
        });
        System.out.println(".");
    }

    public long simulateSerial(int simulations) throws Exception {
        ArrayList<Long> results = new ArrayList<>(simulations);
        for (int i = 0; i < simulations; i++) {
            var ini = new Date().getTime();
            this.simulate();
            results.add(new Date().getTime() - ini);
            System.out.print(". ");
        }
        System.out.println(".");
        results.sort(Comparator.naturalOrder());
        if (simulations % 2 == 0) {
            int i = simulations / 2;
            return (results.get(i - 1) + results.get(i)) / 2;
        }
        int i = simulations / 2;
        return results.get(i);
    }

    public void simulate() throws Exception {
        int idxItemToBuy = this.randomGenerator.generateRandom(16).intValue() % this.items.size();
        BigInteger itemKey = this.simulateObliviousTransfer(obliviousTransferKeys, idxItemToBuy);
        Item itemToBuy = this.items.get(idxItemToBuy);  // Just to test the values.
        BigInteger itemValue = this.buyItem(idxItemToBuy, itemKey, bitKEYS);
        if (!itemValue.equals(itemToBuy.value)) throw new Exception("The item value is different from the original.");
    }

    // Simulem el protocol de Oblivious Transfer que equival a una signatura cega.
    private BigInteger simulateObliviousTransfer(List<BigInteger> obliviousTransferKeys, int idxItemToBuy) throws Exception {
        Hash hash = new Hash();
        AES aes = new AES();
        BigInteger hashItem;
        String key;
        // CLIENT
        hashItem = hash.computeHash(String.valueOf(idxItemToBuy)).modPow(this.rsa.d, this.rsa.N);
        BigInteger blindFactor = this.randomGenerator.generateRandom(this.rsa.N);
        BigInteger blindedMessage = this.rsa.blindMessage(hashItem, blindFactor);

        // SERVIDOR -> obliviousTransferKeysSignatura
        BigInteger blindedMessageSigned = this.rsa.sign(blindedMessage);

        // CLIENT -> Unblind signatura per obtenir la key i desxifrar el contingut.
        key = this.rsa.UnblindSignature(blindedMessageSigned, blindFactor).toString();
        return new BigInteger(aes.decrypt(encryptedOTItems.get(idxItemToBuy).bytes, key));
    }

    private BigInteger buyItem(int idxItemToBuy, BigInteger obliviousTransferKey, List<BigInteger> bitKEYS) throws Exception {
        var itemToBuy = items.get(idxItemToBuy);
        List<BigInteger> boughtKEYS = new ArrayList<>();
        for (int b = 0; b < itemToBuy.priceInBits.length(); b++) {
            if (itemToBuy.priceInBits.charAt(b) == '1') {
                ECoin valuedECoin = this.valuedECoins.get(b);
                BigInteger keyBit = this.spendECoin(valuedECoin, obliviousTransferKey.modPow(bitKEYS.get(b), dsa.N));
                boughtKEYS.add(keyBit);
            } else {
                ECoin noValuedECoin = this.noValuedECoins.get(b);
                // actually the modPow should be done in the server. This is a hack in an
                // already dirty code.
                this.sendServerEcoin(noValuedECoin, obliviousTransferKey);
            }
        }
        // test keys
        var itemToBuyKey = boughtKEYS.stream().reduce((a, b) -> a.multiply(b).mod(dsa.N)).orElseThrow();
        var decryptedValue = new AES().decrypt(itemToBuy.cypherValue, itemToBuyKey.toString());
        return new BigInteger(decryptedValue);
    }

    private BigInteger spendECoin(ECoin eCoin, BigInteger dataToBuy) throws Exception {
        ECIES.Result cypherData = sendServerEcoin(eCoin, dataToBuy);
        return ecies.decypher(cypherData, eCoin.v_r);
    }

    private ECIES.Result sendServerEcoin(ECoin eCoin, BigInteger dataToBuy) throws Exception {
        Date timestamp = new Date();
        Signature signature = eCoin.signTimestamp(timestamp);
        return this.server.spendECoin(eCoin.M_s, eCoin.M_r, eCoin.S, timestamp, signature, dataToBuy);
    }

    private List<BigInteger> generateObliviousTransferKeys() {
        return this.items.stream().map(this::generateKeyForItem).collect(Collectors.toList());
    }

    private List<BigInteger> generateBitKEYS() {
        List<BigInteger> result = new ArrayList<>();
        for (int i = 0; i < Item.M; i++)
            result.add(Item.generateRandomKey());
        return result;
    }

    private BigInteger generateKeyForItem(Item item) {
        return IntStream.range(0, item.priceInBits.length())
                .filter(i -> item.priceInBits.charAt(i) == '1')
                .mapToObj(bitKEYS::get)
                .reduce((a, b) -> a.add(b).mod(dsa.q))
                .map(exp -> item.key.modPow(exp, dsa.N))
                .orElseThrow();
    }
}
