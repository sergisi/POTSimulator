package POT.Entities;

import Crypto.RSA;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ItemTest {

    @Test
    void testIfEcoinMakesSense() throws Exception{
        SimulatorSettings settings = new SimulatorSettings();
        RSA rsa = new RSA();
        var item = new Item(settings, rsa);
        item.initialize();
        System.out.println(item);
        System.out.println(item.priceInBits);

    }

}