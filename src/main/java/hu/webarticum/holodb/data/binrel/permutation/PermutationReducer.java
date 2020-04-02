package hu.webarticum.holodb.data.binrel.permutation;

import java.math.BigInteger;

public class PermutationReducer implements PermutationDecorator {

    private final Permutation base;
    
    private final BigInteger size;
    
    
    public PermutationReducer(Permutation base, BigInteger size) {
        if (size.compareTo(base.size()) > 0) {
            throw new IllegalArgumentException("New size can not be larger than size of base");
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
        BigInteger value = index;
        do {
            value = base.at(value);
        } while (value.compareTo(size) >= 0);
        return value;
    }

    @Override
    public BigInteger indexOf(BigInteger value) {
        BigInteger index = value;
        do {
            index = base.indexOf(index);
        } while (index.compareTo(size) >= 0);
        return index;
    }

    @Override
    public Permutation getBase() {
        return base;
    }

}
