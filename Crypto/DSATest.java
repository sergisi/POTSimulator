package Crypto;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

class DSATest {

    @Test
    void testProp() {
        var dsa = new DSA();
        var res = dsa.g.modPow(dsa.q, dsa.N);
        assertEquals(BigInteger.ONE, res);

    }
}