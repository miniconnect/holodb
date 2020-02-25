package hu.webarticum.holodb.data.binrel.permutation;

import java.math.BigInteger;

public class PermutationInverter implements PermutationDecorator {

    private final Permutation base;
    
    
    public PermutationInverter(Permutation base) {
        if (!base.isReversible()) {
            throw new IllegalArgumentException("Base is not reversible");
        }
        
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
    public boolean isReversible() {
        return true;
    }

    @Override
    public BigInteger indexOf(BigInteger value) {
        return base.at(value);
    }

    @Override
    public Permutation getBase() {
        return base;
    }

}
