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

public class ManagerPOT {
    /**
     * The server ones for bitKeys and Oblivious Transfer are stored in the class. The client Recomputes them in the
     * methods.
     */
    public List<Item> items;
    public List<ECoin> valuedECoins, noValuedECoins;
    protected SimulatorSettings settings;
    protected RSA rsa;
    protected ECIES ecies;
    protected RandomGenerator randomGenerator;
    protected Server server;

    public ManagerPOT(SimulatorSettings settings, RSA rsa) {
        this.settings = settings;
        this.rsa = rsa;
        this.randomGenerator = new RandomGenerator();
        this.ecies = new ECIES(this.settings.EC);
        this.items = new ArrayList<>();
        this.valuedECoins = new ArrayList<>();
        this.noValuedECoins = new ArrayList<>();
    }

    private static long extractMedian(ArrayList<Long> results) {
        results.sort(Comparator.naturalOrder());
        var size = results.size();
        if (size % 2 == 0) {
            int i = size / 2;
            return (results.get(i - 1) + results.get(i)) / 2;
        }
        int i = size / 2;
        return results.get(i);
    }

    public void initialize() throws Exception {
        this.server = new Server(settings, rsa);

        for (int i = 0; i < this.settings.MAX_TICKETS; i++) {
            Item item = new Item(this.settings, this.rsa);
            item.initialize();
            this.items.add(item);
        }

        for (int i = 0; i < Item.M; i++) {
            ECoin valuedECoin = new ECoin(this.settings, this.rsa);
            valuedECoin.initAsValued();
            valuedECoin.S = this.rsa.sign(valuedECoin.M);
            this.valuedECoins.add(valuedECoin);
        }

        for (int i = 0; i < Item.M; i++) {
            ECoin noValuedECoin = new ECoin(this.settings, this.rsa);
            noValuedECoin.initAsNoValued();
            this.noValuedECoins.add(noValuedECoin);
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

    public SimulationResult simulateSerial(int simulations) throws Exception {
        ArrayList<Long> results = new ArrayList<>(simulations);
        ArrayList<Long> timeOfOT = new ArrayList<>(simulations);
        for (int i = 0; i < simulations; i++) {
            var ini = new Date().getTime();
            var time = this.simulate();
            results.add(new Date().getTime() - ini);
            timeOfOT.add(time);
            System.out.print(". ");
        }
        System.out.println(".");
        return new SimulationResult(extractMedian(results), extractMedian(timeOfOT));

    }

    public long simulate() throws Exception {
        // This has to be changed
        int idxItemToBuy = this.randomGenerator.generateRandom(16).intValue() % this.items.size();
        var ini = new Date().getTime();
        List<BigInteger> bitKEYS = this.generateBitKEYS();
        List<BigInteger> obliviousTransferKeys = this.generateObliviousTransferKeys(bitKEYS);
        Item itemToBuy = this.items.get(idxItemToBuy);
        BigInteger obliviousTransferKey = this.simulateObliviousTransfer(obliviousTransferKeys, idxItemToBuy);
        BigInteger itemValue = this.buyItem(itemToBuy, obliviousTransferKey, bitKEYS);
        var time = new Date().getTime() - ini;
        if (!itemValue.equals(itemToBuy.value))
            throw new Exception("The item value is different from the original.");
        return time;
    }

    // Simulem el protocol de Oblivious Transfer que equival a una signatura cega.
    private BigInteger simulateObliviousTransfer(List<BigInteger> obliviousTransferKeys, int idxItemToBuy) throws Exception {
        Hash hash = new Hash();
        AES aes = new AES();
        byte[] itemToBuyEncrypted = new byte[0];
        ArrayList<String> keyList = new ArrayList<>();
        BigInteger hashItem;
        String key;

        // TODO @sergisi: have to change this
        // SERVIDOR
        for (int idxItem = 0; idxItem < obliviousTransferKeys.size(); idxItem++) {
            BigInteger obliviousTransferKey = obliviousTransferKeys.get(idxItem);
            hashItem = hash.computeHash(String.valueOf(idxItem)).modPow(this.rsa.d, this.rsa.N);
            key = this.rsa.sign(hashItem).toString();
            keyList.add(key);

            byte[] itemEncrypted = aes.encrypt(obliviousTransferKey.toString(), key);
            if (idxItem == idxItemToBuy) itemToBuyEncrypted = itemEncrypted;
        }

        // CLIENT
        hashItem = hash.computeHash(String.valueOf(idxItemToBuy)).modPow(this.rsa.d, this.rsa.N);
        BigInteger blindFactor = this.randomGenerator.generateRandom(this.rsa.N);
        BigInteger blindedMessage = this.rsa.blindMessage(hashItem, blindFactor);

        // SERVIDOR -> Signatura
        BigInteger blindedMessageSigned = this.rsa.sign(blindedMessage);

        // CLIENT -> Unblind signatura per obtenir la key i desxifrar el contingut.
        key = this.rsa.UnblindSignature(blindedMessageSigned, blindFactor).toString();
        if (!key.equals(keyList.get(idxItemToBuy))) {
            throw new Exception("Key obtained is not the same as key encrypted");
        }

        return new BigInteger(aes.decrypt(itemToBuyEncrypted, key));
    }

    private BigInteger buyItem(Item itemToBuy, BigInteger obliviousTransferKey, List<BigInteger> bitKEYS) throws Exception {
        List<BigInteger> buyedKEYS = new ArrayList<>();
        for (int b = 0; b < Item.M; b++) {
            if (itemToBuy.priceInBits.charAt(b) == '1') {
                ECoin valuedECoin = this.valuedECoins.get(b);
                BigInteger keyBit = this.spendECoin(valuedECoin, bitKEYS.get(b));
                buyedKEYS.add(keyBit);
            } else {
                ECoin noValuedECoin = this.noValuedECoins.get(b);
                // Es compra la clau amb una moneda sense valor i per tant no se'n fa res.
                this.sendServerEcoin(noValuedECoin, bitKEYS.get(b));
            }
        }
        BigInteger itemToBuyKey = this.generateTemporalItemKey(buyedKEYS, obliviousTransferKey);
        String decryptedValue = new AES().decrypt(itemToBuy.cypherValue, itemToBuyKey.toString());
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

    private List<BigInteger> generateObliviousTransferKeys(List<BigInteger> bitKEYS) throws Exception {
        return this.items.stream().map((item ->
                generateTemporalItemKey(IntStream.range(0, item.priceInBits.length())
                        .filter(i -> item.priceInBits.charAt(i) == '1')
                        .mapToObj(bitKEYS::get)
                        .collect(Collectors.toList()), item.key))).collect(Collectors.toList());
    }

    public BigInteger generateTemporalItemKey(List<BigInteger> bitKEYS, BigInteger initialKey) {
        var res = bitKEYS.stream().reduce(BigInteger::xor);
        return res.map(bigInteger -> bigInteger.xor(initialKey)).orElse(initialKey);
    }

    private List<BigInteger> generateBitKEYS() {
        List<BigInteger> result = new ArrayList<>();
        for (int i = 0; i < Item.M; i++)
            result.add(Item.generateRandomKey());
        return result;
    }
}
