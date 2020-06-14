package hu.webarticum.holodb.core.data.source;

import java.math.BigInteger;

import hu.webarticum.holodb.core.data.binrel.monotonic.Monotonic;
import hu.webarticum.holodb.core.data.selection.Range;

public class MonotonicValueSource<T> implements SortedValueSource<T> {
    
    private final SortedValueSource<T> baseSource;
    
    private final Monotonic monotonic;

    
    public MonotonicValueSource(SortedValueSource<T> baseSource, Monotonic monotonic) {
        if (!monotonic.imageSize().equals(baseSource.size())) {
            throw new IllegalArgumentException("Image size must be equal to size of base source");
        }
        
        this.baseSource = baseSource;
        this.monotonic = monotonic;
    }
    
    
    @Override
    public BigInteger size() {
        return monotonic.size();
    }

    @Override
    public T at(BigInteger index) {
        return baseSource.at(monotonic.at(index));
    }

    @Override
    public boolean isUnique() {
        return false;
    }

    @Override
    public Range find(T value) {
        return adaptRange(baseSource.find(value));
    }

    @Override
    public Range findBetween(T minValue, boolean minInclusive, T maxValue, boolean maxInclusive) {
        return adaptRange(baseSource.findBetween(minValue, minInclusive, maxValue, maxInclusive));
    }
    
    private Range adaptRange(Range baseRange) {
        Range fromRange = monotonic.indicesOf(baseRange.getFrom());
        Range toRange = baseRange.isEmpty() ? fromRange : monotonic.indicesOf(baseRange.getUntil().subtract(BigInteger.ONE));
        return Range.fromUntil(fromRange.getFrom(), toRange.getUntil());
    }

}
