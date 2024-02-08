import Crypto.DSA;
import Crypto.RSA;
import POT.Entities.Item;
import POT.Entities.SimulatorSettings;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ManagerPOTImprovedTest {
    static ManagerPOTImproved manager;

    @BeforeAll
    static void setUp() throws Exception {
        RSA rsa = new RSA();
        DSA dsa = new DSA();
        SimulatorSettings settings = new SimulatorSettings();
        int items = 10;
        int M = 16;
        settings.MAX_TICKETS = items;
        manager = new ManagerPOTImproved(settings, rsa, dsa);
        Item.M = M;
        Item.MAX_PRICE = (int) (Math.pow(2, M) - 1);
        manager.initialize();
    }

    @Test
    void testKeyGeneration() {
        var item = manager.items.get(2);
        assertEquals(BigInteger.ONE, item.key.modPow(manager.dsa.q, manager.dsa.N));
        var expectedKey = IntStream.range(0, item.priceInBits.length())
                .filter(i -> item.priceInBits.charAt(i) == '1')
                .mapToObj(manager.bitKEYS::get)
                .map(exp -> item.key.modPow(exp, manager.dsa.N))
                .reduce((a, b) -> a.multiply(b).mod(manager.dsa.N)).orElseThrow().mod(manager.dsa.N);

        var sumOptimization = IntStream.range(0, item.priceInBits.length())
                .filter(i -> item.priceInBits.charAt(i) == '1')
                .mapToObj(manager.bitKEYS::get)
                .reduce((a, b) -> a.add(b).mod(manager.dsa.q))
                .map(exp -> item.key.modPow(exp, manager.dsa.N))
                .orElseThrow();
        assertEquals(sumOptimization, expectedKey);
        assertEquals(manager.obliviousTransferKeys.get(2), expectedKey);
    }


}