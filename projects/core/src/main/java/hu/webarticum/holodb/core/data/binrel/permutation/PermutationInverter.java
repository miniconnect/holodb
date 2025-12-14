package hu.webarticum.holodb.core.data.binrel.permutation;

import hu.webarticum.miniconnect.lang.LargeInteger;

public class PermutationInverter implements PermutationDecorator {

    private final Permutation base;


    public PermutationInverter(Permutation base) {
        this.base = base;
    }


    @Override
    public LargeInteger size() {
        return base.size();
    }

    @Override
    public LargeInteger at(LargeInteger index) {
        return base.indexOf(index);
    }

    @Override
    public LargeInteger indexOf(LargeInteger value) {
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
