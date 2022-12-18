package POT.Entities;

import Crypto.RSA;
import Helpers.HelperString;
import cat.udl.cig.cryptography.signers.ECDSA;
import cat.udl.cig.cryptography.signers.Signature;
import cat.udl.cig.ecc.ECPoint;

import cat.udl.cig.ecc.GeneralECPoint;
import cat.udl.cig.fields.PrimeField;
import cat.udl.cig.fields.PrimeFieldElement;
import cat.udl.cig.fields.RingElement;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;

public class ECoin extends POTEntity {

	private boolean isValued;

	public BigInteger v_s, v_r;
	public GeneralECPoint Q_s, Q_r;
	public BigInteger M_s, M_r, M, S;

	public ECoin(SimulatorSettings settings, RSA rsa) throws Exception {
		super(settings, rsa);

		this.isValued = false;

		this.S = BigInteger.ZERO;
		this.M_s = BigInteger.ZERO;
		this.M_r = BigInteger.ZERO;
		this.M = BigInteger.ZERO;

		this.Q_s = null;
		this.Q_r = null;
	}

	public boolean getIsValued() {
		return (this.isValued);
	}

	public void initAsValued() throws Exception {

		boolean ok = false;

		while (!ok) {

			this.v_s = settings.EC.getRandomExponent();
			this.v_r = settings.EC.getRandomExponent();

			this.Q_s = this.generatorEC.pow(v_s);
			this.Q_r = this.generatorEC.pow(v_r);

			this.M_s = this.encodePoint(this.Q_s);
			this.M_r = this.encodePoint(this.Q_r);

			this.M = this.M_s.xor(this.M_r);
			ok = this.M.compareTo(rsa.N) == -1;
		}

		this.isValued = true;
	}

	public void initAsNoValued() throws Exception {

		boolean ok = false;

		while (!ok) {


			this.S = this.randomGenerator.generateRandom(this.settings.SIZE_KEY).mod(this.rsa.N);
			this.M = this.rsa.simulateSign(S);

			this.v_s = this.settings.EC.getRandomExponent();
			this.Q_s = this.generatorEC.pow(this.v_s);

			this.M_s = this.encodePoint(this.Q_s);
			this.M_r = this.M.xor(this.M_s);

			this.Q_r = this.decodePoint(this.M_r);
			ok = this.Q_r != null;

			this.v_r = BigInteger.ONE;
		}

		this.isValued = false;
	}

	public void initPublicPart(BigInteger M_s, BigInteger M_r, BigInteger S) throws Exception {

		this.M_s = M_s;
		this.M_r = M_r;
		this.S = S;

		this.M = this.M_s.xor(this.M_r);

		this.Q_s = this.decodePoint(this.M_s);
		this.Q_r = this.decodePoint(this.M_r);
	}

	public void initAux() throws Exception {

		boolean ok = false;

		while (!ok) {

			this.v_s = settings.EC.getRandomExponent();
			this.Q_s = this.generatorEC.pow(v_s);
			this.M_s = this.encodePoint(this.Q_s);

			this.v_r = settings.EC.getRandomExponent();
			this.Q_r = this.generatorEC.pow(v_r);
			this.M_r = this.encodePoint(this.Q_r);

			this.M = this.M_s.xor(this.M_r);
			ok = this.M.compareTo(rsa.N) == -1;
		}
	}

	public void initPublicPartAux(BigInteger M_s, BigInteger M_r, BigInteger M, BigInteger S) throws Exception {

		this.M_s = M_s;
		this.M_r = M_r;
		this.M = M;
		this.S = S;

		if (!this.M.equals(this.M_s.xor(this.M_r)))
			throw new Exception("M != M_s XOR M_r");

		this.Q_s = this.decodePoint(M_s);
		this.Q_r = this.decodePoint(M_r);
	}

	public BigInteger encodePoint(GeneralECPoint point) throws Exception {

		BigInteger x = point.getX().getIntValue();
		BigInteger y = point.getY().getIntValue();

		RingElement xElement = new PrimeFieldElement(new PrimeField(this.settings.ECModule), x);

		ArrayList<ECPoint> points = this.settings.EC.getEC().computePointsGivenX(xElement);

		boolean isGreater = false;
		if (y.equals(points.get(0).getY().getIntValue())) {
			isGreater = points.get(0).getY().getIntValue().compareTo(points.get(1).getY().getIntValue()) > 0;
		} else if (y.equals(points.get(1).getY().getIntValue())) {
			isGreater = points.get(1).getY().getIntValue().compareTo(points.get(0).getY().getIntValue()) > 0;
		} else
			throw new Exception("La coordenada Y no coincideix amb cap de les calculades!");

		String binaryYNegative = isGreater ? HelperString.Fill("", 8, '0') : HelperString.Fill("", 8, '1');

		String binaryX = x.toString(2);
		int binaryXLength = binaryX.length();

		if (binaryXLength < this.oaep.M - 8)
			binaryX = HelperString.Fill("", this.oaep.M - 8 - binaryXLength, '0') + binaryX;

		BigInteger result = this.oaep.encode(binaryYNegative + binaryX);

		return (result);
	}

	public GeneralECPoint decodePoint(BigInteger encodedMessage) throws Exception {

		String decodedMessage = this.oaep.decode(encodedMessage);

		boolean isGreater = decodedMessage.substring(0, 8).equals("00000000");
		BigInteger x = new BigInteger(decodedMessage.substring(8, this.oaep.M), 2);

		RingElement xElement = new PrimeFieldElement(new PrimeField(this.settings.ECModule), x);

		ArrayList<ECPoint> points = this.settings.EC.getEC().computePointsGivenX(xElement);

		if (points == null || points.size() == 0)
			return null;

	 
		BigInteger y;

		if (points.get(0).getY().getIntValue().compareTo(points.get(1).getY().getIntValue()) > 0) {
			y = isGreater ? points.get(0).getY().getIntValue() : points.get(1).getY().getIntValue();
		} else
			y = isGreater ? points.get(1).getY().getIntValue() : points.get(0).getY().getIntValue();

	 

		RingElement yElement = new PrimeFieldElement(new PrimeField(this.settings.ECModule), y);

		GeneralECPoint result = new GeneralECPoint(this.settings.EC.getEC(), xElement, yElement,
				this.settings.ECCardinality);

		return (result);
	}

	public Signature signTimestamp(Date dateTime) {
		String message = dateTime.toString() + this.M_s.toString() + this.M_r.toString();

		ECDSA ecdsa = new ECDSA(settings.EC, this.generatorEC, this.Q_s, this.v_s);
		Signature result = ecdsa.sign(message);

		return (result);
	}

	public boolean isPublicEquals(ECoin value) {

		boolean result = this.Q_s.equals(value.Q_s) && this.Q_r.equals(value.Q_r) && this.M_s.equals(value.M_s)
				&& this.M_r.equals(value.M_r) && this.M.equals(value.M);

		return (result);
	}

	public ECoin clonar() throws Exception {
		ECoin result = new ECoin(this.settings, this.rsa);

		result.isValued = this.isValued;

		result.v_s = this.v_s;
		result.v_r = this.v_r;

		result.Q_s = this.Q_s;
		result.Q_r = this.Q_r;
		result.M_s = this.M_s;
		result.M_r = this.M_r;
		result.M = this.M;
		result.S = this.S;

		return (result);
	}
}
