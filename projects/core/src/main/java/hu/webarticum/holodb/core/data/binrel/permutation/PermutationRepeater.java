package hu.webarticum.holodb.core.data.binrel.permutation;

import hu.webarticum.miniconnect.lang.LargeInteger;

public class PermutationRepeater implements PermutationDecorator {

    private final Permutation base;
    
    private final LargeInteger size;
    
    
    public PermutationRepeater(Permutation base, LargeInteger size) {
        if (size.isNonPositive() || !size.isDivisibleBy(base.size())) {
            throw new IllegalArgumentException("New size must be a positive multiple of base size");
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
        LargeInteger baseSize = base.size();
        LargeInteger innerValue = base.at(index.mod(baseSize));
        LargeInteger startValue = index.divide(baseSize).multiply(baseSize);
        return startValue.add(innerValue);
    }

    @Override
    public LargeInteger indexOf(LargeInteger value) {
        LargeInteger baseSize = base.size();
        LargeInteger innerIndex = base.indexOf(value.mod(baseSize));
        LargeInteger startIndex = value.divide(baseSize).multiply(baseSize);
        return startIndex.add(innerIndex);
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
