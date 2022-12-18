package Crypto;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

import Helpers.HelperBit;

public class RandomGenerator {

	private Random random;

	public RandomGenerator() {
		this.random = new SecureRandom();
	}

	public BigInteger generateRandom(int bits) {
		BigInteger result = new BigInteger(bits, this.random);

		return (result);
	}

	public BigInteger generateRandom(BigInteger modulus) {
		
		int bits = HelperBit.GetBits(modulus);
		BigInteger result = new BigInteger(bits, this.random).mod(modulus);

		return (result);
	}

	public BigInteger generatePrime(int bits, int certainty) {

		BigInteger result = new BigInteger(bits, certainty, this.random);

		return (result);
	}

}