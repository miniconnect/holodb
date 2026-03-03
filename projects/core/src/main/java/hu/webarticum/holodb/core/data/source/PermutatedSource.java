package hu.webarticum.holodb.core.data.source;

import java.util.Optional;

import hu.webarticum.holodb.core.data.binrel.permutation.Permutation;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class PermutatedSource<T> implements Source<T> {

    private final Source<T> baseSource;

    private final Permutation permutation;


    public PermutatedSource(Source<T> baseSource, Permutation permutation) {
        LargeInteger baseSourceSize = baseSource.size();
        LargeInteger permutationSize = permutation.size();
        if (!baseSourceSize.equals(permutationSize)) {
            throw new IllegalArgumentException(String.format(
                    "Unmatching sizes (baseSource size: %d, permutation size: %d)",
                    baseSourceSize.bigIntegerValue(),
                    permutationSize.bigIntegerValue()));
        }

        this.baseSource = baseSource;
        this.permutation = permutation;
    }


    @Override
    public Class<?> type() {
        return baseSource.type();
    }

    @Override
    public LargeInteger size() {
        return baseSource.size();
    }

    @Override
    public T get(LargeInteger index) {
        LargeInteger permutatedIndex = permutation.indexOf(index);
        return baseSource.get(permutatedIndex);
    }

    @Override
    public Optional<ImmutableList<T>> possibleValues() {
        return baseSource.possibleValues();
    }

}
