package hu.webarticum.holodb.data.binrel.permutation;

import java.math.BigInteger;

public class BruteForcePermutationReverser implements PermutationDecorator {

    private final Permutation base;
    
    
    public BruteForcePermutationReverser(Permutation base) {
        this.base = base;
    }
    
    
    @Override
    public BigInteger size() {
        return base.size();
    }

    @Override
    public BigInteger at(BigInteger index) {
        return base.at(index);
    }

    @Override
    public boolean isReversible() {
        return true;
    }

    @Override
    public BigInteger indexOf(BigInteger value) {
        BigInteger size = base.size();
        BigInteger incrementor = BigInteger.valueOf(1);
        for (
                BigInteger index = BigInteger.valueOf(0);
                index.compareTo(size) < 0;
                index = index.add(incrementor)) {
            BigInteger currentValue = base.at(index);
            if (currentValue.equals(value)) {
                return index;
            }
        }
        throw new IllegalStateException("Value was not found");
    }

    @Override
    public Permutation getBase() {
        return base;
    }

    
    
}
