package hu.webarticum.holodb.core.data.binrel.permutation;

import hu.webarticum.miniconnect.lang.LargeInteger;

public class IdentityPermutation implements Permutation {

    private final LargeInteger size;
    
    
    public IdentityPermutation(LargeInteger size) {
        this.size = size;
    }
    
    
    @Override
    public LargeInteger size() {
        return size;
    }

    @Override
    public LargeInteger at(LargeInteger index) {
        return index;
    }

    @Override
    public LargeInteger indexOf(LargeInteger value) {
        return value;
    }

    @Override
    public IdentityPermutation resized(LargeInteger size) {
        return new IdentityPermutation(size);
    }

}
