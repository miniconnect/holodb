package hu.webarticum.holodb.data.binrel.permutation;

import java.math.BigInteger;

public class PermutationExtender implements PermutationDecorator {

    private final Permutation base;
    
    private final BigInteger size;
    
    
    public PermutationExtender(Permutation base, BigInteger size) {
        if (size.compareTo(base.size()) < 0) {
            throw new IllegalArgumentException("");
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
        return base.at(index.mod(base.size()));
    }

    @Override
    public boolean isReversible() {
        return base.isReversible();
    }

    @Override
    public BigInteger indexOf(BigInteger value) {
        return base.indexOf(value.mod(base.size()));
    }

    @Override
    public Permutation getBase() {
        return base;
    }

}
