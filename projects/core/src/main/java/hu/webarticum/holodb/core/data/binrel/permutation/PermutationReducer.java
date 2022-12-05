package hu.webarticum.holodb.core.data.binrel.permutation;

import hu.webarticum.miniconnect.lang.LargeInteger;

public class PermutationReducer implements PermutationDecorator {

    private final Permutation base;
    
    private final LargeInteger size;
    
    
    public PermutationReducer(Permutation base, LargeInteger size) {
        if (size.compareTo(base.size()) > 0) {
            throw new IllegalArgumentException("New size can not be larger than size of base");
        }

        this.base = base;
        this.size = size;
    }
    
    
    @Override
    public LargeInteger size() {
        return size;
    }

    @Override
    public LargeInteger at(LargeInteger index) {
        LargeInteger value = index;
        do {
            value = base.at(value);
        } while (value.compareTo(size) >= 0);
        return value;
    }

    @Override
    public LargeInteger indexOf(LargeInteger value) {
        LargeInteger index = value;
        do {
            index = base.indexOf(index);
        } while (index.compareTo(size) >= 0);
        return index;
    }

    @Override
    public Permutation getBase() {
        return base;
    }

    @Override
    public Permutation resized(LargeInteger size) {
        return base.resized(size);
    }
    
}
