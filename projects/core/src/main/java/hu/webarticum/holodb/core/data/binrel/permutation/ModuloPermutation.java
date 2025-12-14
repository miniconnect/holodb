package hu.webarticum.holodb.core.data.binrel.permutation;

import hu.webarticum.holodb.core.data.random.TreeRandom;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class ModuloPermutation implements Permutation {

    private final LargeInteger size;

    private final LargeInteger prime;

    private final LargeInteger diff;

    private final LargeInteger inverseDiff;

    private final LargeInteger primeModInverse;


    public ModuloPermutation(TreeRandom treeRandom, LargeInteger size) {
        this.size = size;
        if (size.isZero()) {
            this.prime = LargeInteger.ZERO;
            this.diff = LargeInteger.ZERO;
            this.inverseDiff = LargeInteger.ZERO;
            this.primeModInverse = LargeInteger.ZERO;
        } else {
            this.diff = calculateDiff(treeRandom, size);
            this.prime = calculateRelativePrime(treeRandom, size);
            this.inverseDiff = size.subtract(diff);
            this.primeModInverse = this.prime.modInverse(size);
        }
    }

    private static LargeInteger calculateDiff(TreeRandom treeRandom, LargeInteger size) {
        return treeRandom.getNumber(size);
    }

    private static LargeInteger calculateRelativePrime(TreeRandom treeRandom, LargeInteger size) {
        LargeInteger seven = LargeInteger.of(7L);
        LargeInteger p = treeRandom.getNumber(size);
        int i = 0;
        while (i < 4 && !size.gcd(p).equals(LargeInteger.ONE)) {
            p = p.add(seven);
            i++;
        }
        while (!size.gcd(p).equals(LargeInteger.ONE)) {
            p = p.nextProbablePrime();
        }
        return p.mod(size);
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
        LargeInteger multiplier;
        if (value.isGreaterThan(diff)) {
            multiplier = value.subtract(diff);
        } else {
            multiplier = value.add(inverseDiff);
        }
        return primeModInverse.multiply(multiplier).mod(size);
    }

}
