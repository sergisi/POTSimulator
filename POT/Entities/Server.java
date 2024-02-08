package POT.Entities;

import java.math.BigInteger;
import java.util.Date;

import Crypto.ECIES;
import Crypto.RSA;
import cat.udl.cig.cryptography.signers.Signature;

public class Server extends POTEntity {

	public Server(SimulatorSettings settings, RSA rsa) throws Exception {
		super(settings, rsa);

	}

	public ECIES.Result spendECoin(BigInteger M_s, BigInteger M_r, BigInteger S, Date dateTime, Signature signedTimestamp,
			BigInteger dataToBuy) throws Exception {


		ECoin eCoin = new ECoin(this.settings, this.rsa);
		eCoin.initPublicPart(M_s, M_r, S);

		if(!this.rsa.VerifySign(eCoin.M, eCoin.S))
			throw new Exception("The signed eCoin is invalid!");
		
		if(!this.settings.EC.getEC().isOnCurve(eCoin.Q_r))
			throw new Exception("The Q_r not is a point of EC Curve!");

        return ecies.cypher(dataToBuy, eCoin.Q_r);
	}

}
