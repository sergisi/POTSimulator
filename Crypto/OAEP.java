package Crypto;

import Helpers.HelperBit;
import Helpers.HelperString;

import java.math.BigInteger;

public class OAEP {
	public OAEP() {
		this.IsInitialized = false;
		this.Hash = new Hash();
		this.Random = new RandomGenerator();
	}

	public void Initialize(int m, int k0, int k1, int n) throws Exception {
		if (m + k0 + k1 != n)
			throw new Exception("The values m+k0+k1=n (" + m + "+" + k0 + "+" + k1 + "!=" + n + ")");

		if (n <= 0)
			throw new Exception("The value n must be greater than 0");

		if (n % 8 != 0)
			throw new Exception("The value n must be multiple of 8");

		this.M = m;
		this.K0 = k0;
		this.K1 = k1;
		this.N = n;

		this.IsInitialized = true;
	}

	private RandomGenerator Random;

	public int M;

	public int K0;

	public int K1;

	public int N;

	public boolean IsInitialized;

	private Hash Hash;

	/// <summary>
	/// Encoding (message):
	///
	/// 1. Pad message with k1 zeroes.
	/// 2. Generate a length k0 bitstring r at random.
	/// 3. Seed G with r and generate m + k1 pseudo-random bits, denoted by
	/// G(r).
	/// 4. Let X = (M||0...0) XOR G(r).
	/// 5. Seed H with X and generate k0 pseudo-random bits, denoted by H(X).
	/// 6. Let Y = r XOR H(X).
	/// 7. Return X||Y
	///
	/// </summary>
	public BigInteger encode(BigInteger message) throws Exception {

		String binaryMessage = message.toString(2);
		BigInteger result = this.encode(binaryMessage);

		return (result);
	}

	/// <summary>
	/// Encoding (message):
	///
	/// 1. Pad message with k1 zeroes.
	/// 2. Generate a length k0 bitstring r at random.
	/// 3. Seed G with r and generate m + k1 pseudo-random bits, denoted by
	/// G(r).
	/// 4. Let X = (M||0...0) XOR G(r).
	/// 5. Seed H with X and generate k0 pseudo-random bits, denoted by H(X).
	/// 6. Let Y = r XOR H(X).
	/// 7. Return X||Y
	///
	/// </summary>
	public BigInteger encode(String binaryMessage) throws Exception {

		if (!this.IsInitialized)
			throw new Exception("OAEPPaddingScheme - The instance must be initialized!");

		int m = binaryMessage.length();

		if (m > this.M)
			throw new Exception("The message length is major than M bits");
		if (m < this.M)
			binaryMessage = HelperString.Fill(binaryMessage, this.M, '0');

		// 1. Pad message with k1 zeroes.
		String paddedMessage = HelperString.Fill(binaryMessage, this.K1, '0');

		// 2. Generate a length k0 bitstring r at random.
		String r = this.GenerateRandomBits(this.K0);

		// 3. Seed G with r and generate m + k1 pseudo-random bits, denoted by
		// G(r).
		String G_r = this.GenerateRandomBits(this.M + this.K1, r);

		// 4. Let X = (M||0...0) XOR G(r).
		String zeros = HelperString.Fill("", G_r.length() - paddedMessage.length(), '0');
		String X = this.XOR(paddedMessage + zeros, G_r);

		// 5. Seed H with X and generate k0 pseudo-random bits, denoted by H(X).
		String H_X = this.GenerateRandomBits(this.K0, X);

		// 6. Let Y = r XOR H(X).
		String Y = this.XOR(r, H_X);

		// 7. Return X||Y
		BigInteger result = new BigInteger(X + Y, 2);

		return (result);
	}

	/// <summary>
	/// Decoding (X||Y):
	///
	/// 1. Compute r = Y XOR H(X).
	/// 2. Recover M||0...0 as X XOR G(r).
	/// </summary>
	public BigInteger decodeToBigInteger(BigInteger encodedMessage) throws Exception {

		String message = this.decode(encodedMessage);
		BigInteger result = new BigInteger(message, 2);

		return (result);
	}

	/// <summary>
	/// Decoding (X||Y):
	///
	/// 1. Compute r = Y XOR H(X).
	/// 2. Recover M||0...0 as X XOR G(r).
	/// </summary>
	public String decode(BigInteger encodedMessage) throws Exception {

		if (!this.IsInitialized)
			throw new Exception("OAEPPaddingScheme - The instance must be initialized!");

		String encodedMessageToBits = encodedMessage.toString(2);
		int bitsEncodedMessage = encodedMessageToBits.length();

		if (bitsEncodedMessage > this.N)
			throw new Exception("La longitud en bits del encoded message �s m�s gran que n. Bits encoded message: "
					+ bitsEncodedMessage + " - N: " + this.N);
		if (bitsEncodedMessage < this.N)
			encodedMessageToBits = HelperString.Fill(encodedMessageToBits, this.N, '0');

		String X = encodedMessageToBits.substring(0, this.N - this.K0);
		String Y = encodedMessageToBits.substring(this.N - this.K0);

		String H_X = this.GenerateRandomBits(this.K0, X);

		String r = this.XOR(Y, H_X);

		String G_r = this.GenerateRandomBits(this.M + this.K1, r);

		String paddedMessage = this.XOR(X, G_r);

		String result = paddedMessage.substring(0, this.M);

		return (result);
	}

	private String GenerateRandomBits(int bits) {
		String aux = this.Random.generateRandom(8 * bits).toString();
		if (aux.length() < bits)
			aux += this.Random.generateRandom(8 * bits).toString();

		StringBuilder sb = new StringBuilder();
		for (int b = 0; b < bits; b++)
			sb.append(Integer.parseInt(String.valueOf(aux.charAt(b))) > 5 ? "1" : "0");

		return (sb.toString());
	}

	private String GenerateRandomBits(int bits, String seed) {
		String result = "";

		String newSeed = HelperBit.ToBits(this.Hash.ComputeHash(seed).toString());

		result = newSeed;
		while (result.length() < bits) {
			newSeed = HelperBit.ToBits(this.Hash.ComputeHash(newSeed).toString());
			result += newSeed;
		}

		result = result.substring(0, bits);

		return (result);
	}

	private String XOR(String op1, String op2) throws Exception {
		int op1Length = op1.length();
		int op2Length = op2.length();

		if (op1Length != op2Length)
			throw new Exception(
					"The lenght of the op1 and op2 must be the same. Op1: " + op1Length + " - Op2: " + op2Length);

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < op1Length; i++) {

			if (op1.charAt(i) == op2.charAt(i)) {
				sb.append("0");
			} else
				sb.append("1");
		}

		return (sb.toString());
	}

}
