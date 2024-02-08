package Crypto;

import java.math.BigInteger;

public class RSA {
    public BigInteger N, e, d;

    public RSA() {
        // this.N = new BigInteger(
        // "23466828133827336043520737517677912098277501611015315678937077319884481284637625442408535869878134727301278909831614308267665968924396104728834950374448134948760973866070749401661996793527639111092881544285405018876683166356526371700588409334423250266549311206414924551455308801626572511058962240164120278645743854672395175451388783780707526960582762928444576214936695206150483750161439879332053262983995391563813909891817221465539465026345022548079936654572158053311647417154482620268874685054080579337167080333339217307625734645489957036039664340984958390714516069782916402033386535718679193218388863141920437948387");
        // ?? d'on surt???
        this.N = new BigInteger("2107536413730375319237857287826837685491852878604539244161068775753830453804211252758753618539847297033233461620108975407270738548737886598121988940881624222829598404340674708385509305279025298927247863846080437849546557294493081628302581945219985888141527491335297342710365840606752723019857268871062389291291909684633191733032514723334373311378823035362386724983828385275210185560740356105159375569190223371133431901101917017454499569530063097502107768340660068092458600359002340104173749221268804337351914173363638543953318236660003894226486958161221686011170768849843028756541698781365721153639660951109758703939");
        this.e = new BigInteger("65537");
        this.d = new BigInteger("531957022078030250558198197281406670940174715319228652164615403337349334983738385082248231653663640195977049943083184631384905581171279126388665045090007597144318732999732075409510580708998527470810903211038994810674872984199834540723275562473549392887027904262759794362190392226023521738780825208128447192500022853408803091112918330562206491890631560773270041011257192315663365444083329956262929257969738131804789685057487669123784311214987575131478103515589802880712686223054832443952752324084906534119841377761801093782317342762107120743812397144965493813233827959317417156830906191192290846683043076405795858433");
    }

    public RSA(BigInteger N, BigInteger e, BigInteger d) {
        this.N = N;
        this.e = e;
        this.d = d;
    }

    public BigInteger sign(BigInteger message) throws Exception {
        if (message.compareTo(this.N) >= 0) throw new Exception("The message is bigger than RSA modulus N");
        // S = M^d (mod N).
        return message.modPow(this.d, this.N);
    }

    public boolean VerifySign(BigInteger message, BigInteger signature) throws Exception {
        if (message.compareTo(this.N) >= 0) throw new Exception("The message is bigger than RSA modulus N");
        if (signature.compareTo(this.N) >= 0) throw new Exception("The signature is bigger than RSA modulus N");
        // thatM equals Se (mod N).
        return message.equals(signature.modPow(this.e, this.N));
    }

    public BigInteger simulateSign(BigInteger signature) throws Exception {
        if (signature.compareTo(this.N) >= 0) throw new Exception("The signature is bigger than RSA modulus N");
        return signature.modPow(this.e, this.N);
    }

    public BigInteger blindMessage(BigInteger message, BigInteger blindFactor) throws Exception {
        if (message.compareTo(this.N) >= 0) throw new Exception("The message is bigger than RSA modulus N");
        if (blindFactor.compareTo(this.N) >= 0) throw new Exception("The blindFactor is bigger than RSA modulus N");
        // M0 = M  re (mod N).
        BigInteger re = blindFactor.modPow(this.e, this.N);
        return message.multiply(re).mod(this.N);
    }

    public BigInteger UnblindSignature(BigInteger blindSignature, BigInteger blindFactor) throws Exception {
        if (blindSignature.compareTo(this.N) >= 0)
            throw new Exception("The blindSignature is bigger than RSA modulus N");
        if (blindFactor.compareTo(this.N) >= 0) throw new Exception("The blindFactor is bigger than RSA modulus N");
        BigInteger inverseBlindFactor = blindFactor.modInverse(this.N);
        return blindSignature.multiply(inverseBlindFactor).mod(this.N);
    }

    public RSA clone2() {
        return new RSA(this.N, this.e, this.d);
    }

}
