package POT.Entities;

import cat.udl.cig.ecc.ECPrimeOrderSubgroup;
import cat.udl.cig.ecc.GeneralEC;
import cat.udl.cig.ecc.GeneralECPoint;
import cat.udl.cig.fields.PrimeField;
import cat.udl.cig.fields.PrimeFieldElement;
import cat.udl.cig.fields.RingElement;

import java.math.BigInteger;
import java.util.ArrayList;

public class SimulatorSettings {

	public SimulatorSettings() {
		this.version = "1.00";

		this.SIZE_KEY = 2048;
		this.SIZE_POINT = 224;

		this.MAX_TICKETS = 100;
		 
		this.MAX_POT_SIMULATIONS = 8;

		this.modulus = new BigInteger(
				"10912993990071291317038388166715412483603948165467756744765718154448660365098952303878476546132894085866920071972295836559756372397464061495424728298590286067044885142385846596142805793695326511104152635976446556745655291746854134670372932397952826419666052645996360833040297343348890038185585172188496735915024976681511302689450830973928849615788129930807293670579105006702813966062937680678027860365251401362157355365378670601807590755312820294796025474376175919919190520213790596980552618166576584685666511267158921359611400817651605712226524345560669538240364863568934106874395878749904147196094902886227364639853");
		this.g = new BigInteger("7");
		this.e = new BigInteger("7");

		this.EC = BuildDefaultECC();
	}

	public int MAX_TICKETS;

	public int MAX_ECOINS;
	
	public int MAX_POT_SIMULATIONS;

	public int SLOTS_TICKETS;

	public BigInteger modulus;
	public BigInteger g;

	public int SIZE_KEY;

	public int SIZE_POINT;

	public String version;

	public BigInteger e;

	public BigInteger ECModule;
	public BigInteger ECCardinality;

	public BigInteger ECCoeficientA;
	public BigInteger ECCoeficientB;

	public BigInteger ECGeneralPointX;
	public BigInteger ECGeneralPointY;

	public ECPrimeOrderSubgroup EC;

	private ECPrimeOrderSubgroup BuildDefaultECC() {

		/*********
		 * 224 bits DOCUMENT NIST page 90
		 **********************************************************************/
		this.ECModule = new BigInteger("26959946667150639794667015087019630673557916260026308143510066298881");
		this.ECCardinality = new BigInteger("26959946667150639794667015087019625940457807714424391721682722368061");

		this.ECCoeficientA = new BigInteger("-3");
		this.ECCoeficientB = new BigInteger(
				"b4050a850c04b3abf54132565044b0b7d7bfd8ba270b39432355ffb4".replaceAll("\\s", ""), 16);

		this.ECGeneralPointX = new BigInteger(
				"b70e0cbd6bb4bf7f321390b94a03c1d356c21122343280d6115c1d21".replaceAll("\\s", ""), 16);
		this.ECGeneralPointY = new BigInteger(
				"bd376388b5f723fb4c22dfe6cd4375a05a07476444d5819985007e34".replaceAll("\\s", ""), 16);

		/***************************************************************************************************************/

		PrimeField ring = new PrimeField(this.ECModule);

		RingElement[] coeficients = new RingElement[2];
		coeficients[0] = new PrimeFieldElement(ring, this.ECCoeficientA); 
		coeficients[1] = new PrimeFieldElement(ring, this.ECCoeficientB);

		ArrayList<BigInteger> card = new ArrayList<>();
		card.add(this.ECCardinality);

		GeneralEC curveECC = new GeneralEC(ring, coeficients, card);
		GeneralECPoint generalPoint = new GeneralECPoint(curveECC, new PrimeFieldElement(ring, this.ECGeneralPointX),
				new PrimeFieldElement(ring, this.ECGeneralPointY), this.ECCardinality);

		ECPrimeOrderSubgroup result = new ECPrimeOrderSubgroup(curveECC, this.ECCardinality, generalPoint);

		return (result);
	}

	public String print() {
		String result = "- ITEMS: " + MAX_TICKETS + System.lineSeparator() + "- POT EXECUTIONS: " + MAX_POT_SIMULATIONS
				+ System.lineSeparator() + "- #Bits RSA: " + SIZE_KEY + System.lineSeparator() + "- Module RSA: "
				+ this.modulus + System.lineSeparator() + "- #Bits EC: " + SIZE_POINT + System.lineSeparator()
				+ "- EC: " + this.EC.getEC().toString() + System.lineSeparator() + "- Module: " + this.ECModule
				+ System.lineSeparator();

		return (result);
	}
}
