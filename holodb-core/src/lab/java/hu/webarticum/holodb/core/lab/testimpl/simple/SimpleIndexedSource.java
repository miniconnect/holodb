package hu.webarticum.holodb.core.lab.testimpl.simple;

import java.math.BigInteger;
import java.util.function.BiFunction;
import java.util.function.Function;

import hu.webarticum.holodb.core.data.binrel.monotonic.Monotonic;
import hu.webarticum.holodb.core.data.binrel.permutation.Permutation;
import hu.webarticum.holodb.core.data.selection.PermutatedSelection;
import hu.webarticum.holodb.core.data.selection.Range;
import hu.webarticum.holodb.core.data.selection.Selection;
import hu.webarticum.holodb.core.data.source.IndexedSource;
import hu.webarticum.holodb.core.data.source.SortedSource;

public class SimpleIndexedSource<T> implements IndexedSource<T> {
    
    private final SortedSource<T> baseSource;
    
    private final Monotonic monotonic;
    
    private final Permutation permutation;
    
    
    public SimpleIndexedSource(
            SortedSource<T> baseSource,
            BigInteger targetSize,
            BiFunction<BigInteger, BigInteger, Monotonic> monotonicFactory,
            Function<BigInteger, Permutation> permutationFactory) {

        this.baseSource = baseSource;
        this.monotonic = monotonicFactory.apply(baseSource.size(), targetSize);
        this.permutation = permutationFactory.apply(targetSize);
    }
    
    
    @Override
    public Class<T> type() {
        return baseSource.type();
    }

    @Override
    public BigInteger size() {
        return permutation.size();
    }

    @Override
    public T get(BigInteger index) {
        BigInteger valueIndex = monotonic.at(permutation.at(index));
        return baseSource.get(valueIndex);
    }

    @Override
    public Selection find(T value) {
        Range baseRange = baseSource.find(value);
        Range montonicRange = monotonic.indicesOf(baseRange);
        return new PermutatedSelection(montonicRange, permutation);
    }

    @Override
    public Selection findBetween(
            T minValue, boolean minInclusive, T maxValue, boolean maxInclusive) {
        
        Range baseRange = baseSource.findBetween(minValue, minInclusive, maxValue, maxInclusive);
        Range montonicRange = monotonic.indicesOf(baseRange);
        return new PermutatedSelection(montonicRange, permutation);
    }
    
}
