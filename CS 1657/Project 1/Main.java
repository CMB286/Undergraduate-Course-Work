import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.io.BufferedReader;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
    	long varianceA = algorithmATest();
    	long varianceB = algorithmBTest();

		System.out.println("Variance A: " + varianceA + " Variance B: " + varianceB);
    }

    /*
    *
    * C2
    * Square-and-Multiply Algorithm
	* Right-to-left binary algorithm
	* https://link.springer.com/content/pdf/10.1007%2F3-540-36400-5_22.pdf
	*
    */
    public static void algorithmA(BigInteger base, BigInteger exponent, BigInteger modulus){
		BigInteger r = new BigInteger("1");

		while(exponent.bitLength() > 0){	//While exponent is greater than 0
			if(exponent.testBit(0)){	//Check if bit is 0, 1
				r = r.multiply(base).mod(modulus);	// (r * base) mod(modulus)
			}
			exponent = exponent.shiftRight(1);	// Shift the bit to the right to move to next position

			base = base.multiply(base).mod(modulus); // base^2 mod(modulus)
		}

		//R is our final value
	}

	/*
	 * C3:
	 * Implement a test of Algorithm A for different inputs of the same size
	 */
	public static long algorithmATest(){
		Random rand = new Random();
		BigInteger randomModulus;
		BigInteger randomExp;
		BigInteger randomBase;
		long start, end;
		long[] algAResults = new long[2000];


		for(int i = 0; i < 2000; i++){
			//Generate random values
			randomModulus = new BigInteger(1024, rand);
			randomExp = new BigInteger(1024, rand);
			randomBase = new BigInteger(1024, rand);

			start = System.currentTimeMillis();
			algorithmA(randomBase, randomExp, randomModulus);
			end = System.currentTimeMillis();
			algAResults[i] = end - start;
			//System.out.println(algAResults[i]);
		}

		//Finds mean of both sets
		long aMean = 0;
		for(int i = 0; i < 2000; i++){
			aMean += algAResults[i];
		}
		aMean = aMean / 2000;


		//Subtract mean from each entry then square it
		for(int i = 0; i < 2000; i++){
			algAResults[i] = algAResults[i] - aMean;
			algAResults[i] = algAResults[i] * algAResults[i];
		}

		long varianceA = 0;
		for(int i = 0; i < 2000; i++){
			varianceA += algAResults[i];
		}

		varianceA /= 2000;
		return varianceA;
	}

	/*
	 * C5:
	 * Montgomery Reduction
	 * https://en.wikipedia.org/wiki/Montgomery_modular_multiplication
	 * https://gmplib.org/~tege/modexp-silent.pdf
	 *
	 */
	public static void algorithmB(BigInteger base, BigInteger exp, BigInteger mod){
		// Follow same structure as algorithm A roughly
		while (base.compareTo(mod) >= 0) {
			base = base.subtract(mod);
		}

		BigInteger ONE = new BigInteger("1");
		int k = mod.bitLength();	//k = mod bit length
		BigInteger rrm = ONE.shiftLeft(2 * k).mod(mod);


		BigInteger r = montReduction(rrm, mod, k);
		BigInteger montBase = montReduction(base.multiply(rrm), mod, k);

		while (exp.bitLength() > 0) {
			if (exp.testBit(0)) {
				r = montReduction(r.multiply(montBase), mod, k);
			}
			exp = exp.shiftRight(1);

			montBase = montReduction(montBase.multiply(montBase), mod, k);
		}

		r = montReduction(r, mod, k);
		//R is our final value
	}

	//Perform the REDC operation
	public static BigInteger montReduction(BigInteger m, BigInteger mod, int k){
		BigInteger temp = m;
		for (int i = 0; i < k; i++) {
			if (temp.testBit(0)) {
				temp = temp.add(mod);
			}
			temp = temp.shiftRight(1);	//Divide by 2
		}
		if(temp.compareTo(mod) >= 0) {
			temp = temp.subtract(mod);
		}
		return temp;
	}

	/*
	 * C6:
	 * Implement a test of Algorithm B for different inputs of the same size
	 */
	public static long algorithmBTest(){
		Random rand = new Random();
		BigInteger randomModulus;
		BigInteger randomExp;
		BigInteger randomBase;
		long start, end;
		long[] algBResults = new long[2000];


		for(int i = 0; i < 2000; i++){

			//Generate random values
			randomModulus = new BigInteger(1024, rand);
			randomExp = new BigInteger(1024, rand);
			randomBase = new BigInteger(1024, rand);

			start = System.currentTimeMillis();
			algorithmB(randomBase, randomExp, randomModulus);
			end = System.currentTimeMillis();
			algBResults[i] = end - start;
			//System.out.println(algBResults[i]);
		}

		//Finds mean of both sets
		long  bMean = 0;
		for(int i = 0; i < 2000; i++){
			bMean += algBResults[i];
		}
		bMean /= 2000;

		//Subtract mean from each entry then square it
		for(int i = 0; i < 2000; i++){
			algBResults[i] -= bMean;
			algBResults[i] *= algBResults[i];
		}

		long varianceB = 0;
		for(int i = 0; i < 2000; i++){
			varianceB += algBResults[i];
		}

		varianceB /= 2000;
		return varianceB;
	}
}
