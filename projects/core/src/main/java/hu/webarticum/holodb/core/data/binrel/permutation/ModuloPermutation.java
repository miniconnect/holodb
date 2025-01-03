package hu.webarticum.holodb.core.data.binrel.permutation;

import hu.webarticum.holodb.core.data.random.TreeRandom;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class ModuloPermutation implements Permutation {
    
    private final LargeInteger size;
    
    private final LargeInteger prime;
    
    private final LargeInteger diff;
    
    private final LargeInteger inverseDiff;
    

    public ModuloPermutation(TreeRandom treeRandom, LargeInteger size) {
        this.size = size;
        if (size.equals(LargeInteger.ZERO)) {
            this.prime = LargeInteger.ZERO;
            this.diff = LargeInteger.ZERO;
            this.inverseDiff = LargeInteger.ZERO;
        } else {
            this.diff = calculateDiff(treeRandom, size);
            this.prime = calculatePrime(treeRandom, size);
            this.inverseDiff = size.subtract(diff);
        }
    }

    private static LargeInteger calculateDiff(TreeRandom treeRandom, LargeInteger size) {
        return treeRandom.getNumber(size);
    }

    private static LargeInteger calculatePrime(TreeRandom treeRandom, LargeInteger size) {
        LargeInteger p = treeRandom.getNumber(size);
        while (!size.gcd(p).equals(LargeInteger.ONE)) {
            p = p.nextProbablePrime();
        }
        return p;
    }


    @Override
    public LargeInteger size() {
        return size;
    }

    @Override
    public LargeInteger at(LargeInteger index) {
        return prime.multiply(index).add(diff).mod(size);
    }

    @Override
    public LargeInteger indexOf(LargeInteger value) {
        return prime.modInverse(size).multiply(value.add(inverseDiff)).mod(size);
    }
    
}
