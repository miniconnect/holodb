package hu.webarticum.holodb.core.data.binrel.permutation;

import java.math.BigInteger;

public class PermutationInverter implements PermutationDecorator {

    private final Permutation base;
    
    
    public PermutationInverter(Permutation base) {
        this.base = base;
    }
    
    
    @Override
    public BigInteger size() {
        return base.size();
    }

    @Override
    public BigInteger at(BigInteger index) {
        return base.indexOf(index);
    }

    @Override
    public BigInteger indexOf(BigInteger value) {
        return base.at(value);
    }

    @Override
    public Permutation getBase() {
        return base;
    }
    
    @Override
    public Permutation inverted() {
        return base;
    }

}
