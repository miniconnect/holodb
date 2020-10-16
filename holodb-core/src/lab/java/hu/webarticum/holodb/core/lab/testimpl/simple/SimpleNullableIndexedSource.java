package hu.webarticum.holodb.core.lab.testimpl.simple;

import java.math.BigInteger;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import hu.webarticum.holodb.core.data.binrel.monotonic.Monotonic;
import hu.webarticum.holodb.core.data.binrel.permutation.Permutation;
import hu.webarticum.holodb.core.data.selection.PermutatedSelection;
import hu.webarticum.holodb.core.data.selection.Range;
import hu.webarticum.holodb.core.data.selection.Selection;
import hu.webarticum.holodb.core.data.source.IndexedSource;
import hu.webarticum.holodb.core.data.source.SortedSource;

public class SimpleNullableIndexedSource<T> implements IndexedSource<Optional<T>> {
    
    private final SortedSource<T> baseSource;
    
    private final Monotonic monotonic;
    
    private final Permutation permutation;
    

    public SimpleNullableIndexedSource(
            SortedSource<T> baseSource,
            BigInteger targetSize,
            BigInteger dataSize,
            BiFunction<BigInteger, BigInteger, Monotonic> monotonicFactory,
            Function<BigInteger, Permutation> permutationFactory) {

        this.baseSource = baseSource;
        this.monotonic = monotonicFactory.apply(baseSource.size(), dataSize);
        this.permutation = permutationFactory.apply(targetSize);
    }
    
    
    @SuppressWarnings("unchecked")
    @Override
    public Class<Optional<T>> type() {
        return (Class<Optional<T>>) (Object) Optional.class;
    }

    @Override
    public BigInteger size() {
        return permutation.size();
    }

    @Override
    public Optional<T> get(BigInteger index) {
        BigInteger sortedIndex = permutation.at(index);
        if (sortedIndex.compareTo(monotonic.size()) >= 0) {
            return Optional.empty();
        }

        BigInteger valueIndex = monotonic.at(sortedIndex);
        T value = baseSource.get(valueIndex);
        return Optional.of(value);
    }

    @Override
    public Selection find(Optional<T> value) {
        return new PermutatedSelection(getRangeInMonotonic(value, true), permutation);
    }

    @Override
    public Selection findBetween(
            Optional<T> minValue, boolean minInclusive,
            Optional<T> maxValue, boolean maxInclusive) {

        Range fromRange = getRangeInMonotonic(minValue, true);
        BigInteger from = minInclusive ? fromRange.from() : fromRange.until();
        
        Range untilRange = getRangeInMonotonic(maxValue, true);
        BigInteger until = maxInclusive ? untilRange.until() : untilRange.from();
        
        return new PermutatedSelection(Range.fromUntil(from, until), permutation);
    }
    
    private Range getRangeInMonotonic(Optional<T> value, boolean isMin) {
        if (value == null) { // NOSONAR
            return Range.empty(isMin ? BigInteger.ZERO : size());
        }
        
        if (value.isEmpty()) {
            return Range.fromUntil(monotonic.size(), size());
        }
        
        return baseSource.find(value.get());
    }

}
