package Helpers;

import java.math.BigInteger;

public class HelperXOR {

	public static BigInteger xor(BigInteger v1, BigInteger v2) {
		BigInteger result = v1.xor(v2);

		return (result);
	}

	public static BigInteger xor(byte[] v1, byte[] v2) {
		BigInteger result = new BigInteger(v1).xor(new BigInteger(v2));

		return (result);
	}

	public static String stringXor(String op1, String op2) throws Exception {

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
