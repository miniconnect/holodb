package hu.webarticum.holodb.data.binrel.permutation;

import java.math.BigInteger;

public class PermutationExtender implements PermutationDecorator {

    private final Permutation base;
    
    private final BigInteger size;
    
    
    public PermutationExtender(Permutation base, BigInteger size) {
        if (size.compareTo(base.size()) < 0) {
            throw new IllegalArgumentException("New size can not be smaller than size of base");
        }

        this.base = base;
        this.size = size;
    }
    
    
    @Override
    public BigInteger size() {
        return size;
    }

    @Override
    public BigInteger at(BigInteger index) {
        BigInteger baseSize = base.size();
        BigInteger innerValue = base.at(index.mod(baseSize));
        BigInteger startValue = index.divide(baseSize).multiply(baseSize);
        return startValue.add(innerValue);
    }

    @Override
    public BigInteger indexOf(BigInteger value) {
        BigInteger baseSize = base.size();
        BigInteger innerIndex = base.indexOf(value.mod(baseSize));
        BigInteger startIndex = value.divide(baseSize).multiply(baseSize);
        return startIndex.add(innerIndex);
    }

    @Override
    public Permutation getBase() {
        return base;
    }

}
