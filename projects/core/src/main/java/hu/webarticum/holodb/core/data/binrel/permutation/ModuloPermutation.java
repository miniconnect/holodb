package hu.webarticum.holodb.core.data.binrel.permutation;

import java.math.BigInteger;

import hu.webarticum.holodb.core.data.random.TreeRandom;

public class ModuloPermutation implements Permutation {
    
    private final BigInteger size;
    
    private final BigInteger prime;
    
    private final BigInteger diff;
    
    private final BigInteger inverseDiff;
    

    public ModuloPermutation(TreeRandom treeRandom, BigInteger size) {
        this.size = size;
        if (size.equals(BigInteger.ZERO)) {
            this.prime = BigInteger.ZERO;
            this.diff = BigInteger.ZERO;
            this.inverseDiff = BigInteger.ZERO;
        } else {
            this.diff = calculateDiff(treeRandom, size);
            this.prime = calculatePrime(size, this.diff);
            this.inverseDiff = size.subtract(diff);
        }
    }

    private static BigInteger calculateDiff(TreeRandom treeRandom, BigInteger size) {
        return treeRandom.getNumber(size);
    }

    private static BigInteger calculatePrime(BigInteger size, BigInteger diff) {
        BigInteger p = diff.divide(BigInteger.TWO).add(diff.divide(BigInteger.valueOf(4L)));
        while (!size.gcd(p).equals(BigInteger.ONE)) {
            p = p.nextProbablePrime();
        }
        return p;
    }


    @Override
    public BigInteger size() {
        return size;
    }

    @Override
    public BigInteger at(BigInteger index) {
        return prime.multiply(index).add(diff).mod(size);
    }

    @Override
    public BigInteger indexOf(BigInteger value) {
        return prime.modInverse(size).multiply(value.add(inverseDiff)).mod(size);
    }
    
}
