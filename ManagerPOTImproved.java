import Crypto.*;
import POT.Entities.ECoin;
import POT.Entities.Item;
import POT.Entities.Server;
import POT.Entities.SimulatorSettings;
import cat.udl.cig.cryptography.signers.Signature;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ManagerPOTImproved {


    protected SimulatorSettings settings;
    protected RSA rsa;
    protected ECIES ecies;

    protected RandomGenerator randomGenerator;

    protected Server server;

    public List<Item> items;
    public List<ECoin> valuedECoins, noValuedECoins;

    public ManagerPOTImproved(SimulatorSettings settings, RSA rsa) {

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


    public void simulateAsync(int simulations) {
        IntStream.range(0, simulations).parallel().forEach((id) -> {
                this.simulate();
                System.out.print(". ");
        });
        System.out.println(".");
    }

    public void simulateSerial(int simulations) {
        for (int i = 0; i < simulations; i++) {
            this.simulate();
            System.out.print(". ");
        }
        System.out.println(".");
    }

    public void simulate() {

        try {
            // TODO @sergisi: Move this two lines to init method.
            // This has to be changed
            List<BigInteger> bitKEYS = this.generateBitKEYS(); // K_0, K_1,..,K_M
            List<BigInteger> obliviousTransferKeys = this.generateObliviousTransferKeys(bitKEYS);

            int idxItemToBuy = this.randomGenerator.generateRandom(16).intValue() % this.items.size();
            Item itemToBuy = this.items.get(idxItemToBuy);
            // BigInteger obliviousTransferKey = obliviousTransferKeys.get(idxItemToBuy);

            BigInteger obliviousTransferKey = BigInteger.ZERO;
            obliviousTransferKey = this.simulateObliviousTransfer(obliviousTransferKeys, idxItemToBuy);
            BigInteger itemValue = this.buyItem(itemToBuy, obliviousTransferKey, bitKEYS);
            if (!itemValue.equals(itemToBuy.value))
                throw new Exception("El valor de l'item comprat no correspon a l'original!!!");

        } catch (Exception ex) {
            System.out.println("Some exception was thrown: " + ex);
            ex.printStackTrace();
            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
            for (StackTraceElement stackTraceElement : stackTraceElements)
                System.out.println("StackTrace: " + stackTraceElement.toString());
        }
    }

    // Simulem el protocol de Oblivious Transfer que equival a una signatura cega.
    private BigInteger simulateObliviousTransfer(List<BigInteger> obliviousTransferKeys, int idxItemToBuy) throws Exception {

        Hash hash = new Hash();
        AES aes = new AES();

        byte[] itemToBuyEncrypted = null;
        ArrayList<String> keyList = new ArrayList<>();
        BigInteger hashItem;
        String key;

        // TODO @sergisi: have to change this
        // SERVIDOR
        for (int idxItem = 0; idxItem < obliviousTransferKeys.size(); idxItem++) {
            BigInteger obliviousTransferKey = obliviousTransferKeys.get(idxItem);
            hashItem = hash.ComputeHash(String.valueOf(idxItem)).modPow(this.rsa.d, this.rsa.N);
            key = this.rsa.Sign(hashItem).toString();
            keyList.add(key);

            byte[] itemEncrypted = aes.encrypt(obliviousTransferKey.toString(), key);
            if (idxItem == idxItemToBuy) itemToBuyEncrypted = itemEncrypted;
        }

        // CLIENT
        hashItem = hash.ComputeHash(String.valueOf(idxItemToBuy)).modPow(this.rsa.d, this.rsa.N);
        BigInteger blindFactor = this.randomGenerator.generateRandom(this.rsa.N);
        BigInteger blindedMessage = this.rsa.BlindMessage(hashItem, blindFactor);

        // SERVIDOR -> Signatura
        BigInteger blindedMessageSigned = this.rsa.Sign(blindedMessage);

        // CLIENT -> Unblind signatura per obtenir la key i desxifrar el contingut.
        key = this.rsa.UnblindSignature(blindedMessageSigned, blindFactor).toString();
        if (!key.equals(keyList.get(idxItemToBuy))) {
            throw new Exception("Key obtained is not the same as key encrypted");
        }

        return new BigInteger(aes.decrypt(itemToBuyEncrypted, key));
    }

    private BigInteger buyItem(Item itemToBuy, BigInteger obliviousTransferKey, List<BigInteger> bitKEYS) throws Exception {
        List<BigInteger> buyedKEYS = new ArrayList<>();
        int idxLastValuedECoin = 0;
        int idxLastNoValuedECoin = 0;
        for (int b = 0; b < itemToBuy.priceInBits.length(); b++) {
            if (itemToBuy.priceInBits.charAt(b) == '1') {
                ECoin valuedECoin = this.valuedECoins.get(idxLastValuedECoin);
                BigInteger keyBit = this.spendECoin(valuedECoin, bitKEYS.get(b));
                buyedKEYS.add(keyBit);
                idxLastValuedECoin++;
            } else {
                ECoin noValuedECoin = this.noValuedECoins.get(idxLastNoValuedECoin);
                // Es compra la clau amb una moneda sense valor i per tant no se'n fa res.
                this.sendServerEcoin(noValuedECoin, bitKEYS.get(b));
                idxLastNoValuedECoin++;
            }
        }
        BigInteger itemToBuyKey = this.generateTemporalItemKey(buyedKEYS, obliviousTransferKey, itemToBuy.priceInBits);
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
        return this.items.stream().map((item -> this.generateTemporalItemKey(bitKEYS, item.key, item.priceInBits))).collect(Collectors.toList());
    }

    public BigInteger generateTemporalItemKey(List<BigInteger> bitKEYS, BigInteger initialKey, String bitKeyArray) {
        BigInteger result = BigInteger.ONE;
        boolean isFirst = true;
        for (int b = 0; b < bitKeyArray.length(); b++) {
            if (bitKeyArray.charAt(b) == '1') {
                if (isFirst) {
                    isFirst = false;
                    result = initialKey.xor(bitKEYS.get(b));
                } else result = result.xor(bitKEYS.get(b));
            }
        }
        return result;
    }

    private List<BigInteger> generateBitKEYS() {
        List<BigInteger> result = new ArrayList<>();
        for (int i = 0; i < Item.M; i++)
            result.add(Item.generateRandomKey());
        return result;
    }
}
