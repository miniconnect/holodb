package hu.webarticum.holodb.core.data.binrel.permutation;

import java.util.Arrays;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class MockPermutation implements Permutation {

    private final ImmutableList<LargeInteger> values;


    public MockPermutation(ImmutableList<LargeInteger> values) {
        this.values = values;
    }

    public static MockPermutation of(int... values) {
        return new MockPermutation(
                Arrays.stream(values)
                        .mapToObj(LargeInteger::of)
                        .collect(ImmutableList.createCollector()));
    }


    @Override
    public LargeInteger size() {
        return LargeInteger.of(values.size());
    }

    @Override
    public LargeInteger at(LargeInteger index) {
        return values.get(index.intValueExact());
    }

    @Override
    public LargeInteger indexOf(LargeInteger value) {
        int index = values.indexOf(value);
        if (index == -1) {
            throw new IllegalArgumentException("Value not found: " + value);
        }
        return LargeInteger.of(index);
    }

}
