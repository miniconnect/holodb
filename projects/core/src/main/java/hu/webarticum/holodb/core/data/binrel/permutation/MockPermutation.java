package hu.webarticum.holodb.core.data.binrel.permutation;

import java.math.BigInteger;
import java.util.Arrays;

import hu.webarticum.miniconnect.lang.ImmutableList;

public class MockPermutation implements Permutation {
    
    private final ImmutableList<BigInteger> values;
    

    public MockPermutation(ImmutableList<BigInteger> values) {
        this.values = values;
    }
    
    public static MockPermutation of(int... values) {
        return new MockPermutation(
                Arrays.stream(values)
                        .mapToObj(BigInteger::valueOf)
                        .collect(ImmutableList.createCollector()));
    }


    @Override
    public BigInteger size() {
        return BigInteger.valueOf(values.size());
    }

    @Override
    public BigInteger at(BigInteger index) {
        return values.get(index.intValueExact());
    }

    @Override
    public BigInteger indexOf(BigInteger value) {
        int index = values.indexOf(value);
        if (index == -1) {
            throw new IllegalArgumentException("Value not found: " + value);
        }
        return BigInteger.valueOf(index);
    }
    
}