import Crypto.RSA;
import POT.Entities.Item;
import POT.Entities.SimulatorSettings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagerPOTTest {

    @Test
    void simulate() throws Exception {
        SimulatorSettings settings = new SimulatorSettings();
        RSA rsa = new RSA();
        settings.MAX_TICKETS = 5;
        Item.M = 16;
        Item.MAX_PRICE = (int) (Math.pow(2, 16) - 1);

        ManagerPOT manager = new ManagerPOT(settings, rsa);
        manager.initialize();
        manager.simulate();

    }
}