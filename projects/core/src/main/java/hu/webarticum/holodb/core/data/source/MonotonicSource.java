package hu.webarticum.holodb.core.data.source;

import java.math.BigInteger;

import hu.webarticum.holodb.core.data.binrel.monotonic.Monotonic;
import hu.webarticum.holodb.core.data.selection.Range;

public class MonotonicSource<T> implements SortedSource<T> {
    
    private final SortedSource<T> baseSource;
    
    private final Monotonic monotonic;

    
    public MonotonicSource(SortedSource<T> baseSource, Monotonic monotonic) {
        if (!monotonic.imageSize().equals(baseSource.size())) {
            throw new IllegalArgumentException("Image size must be equal to the size of base source");
        }
        
        this.baseSource = baseSource;
        this.monotonic = monotonic;
    }
    
    
    @Override
    public Class<T> type() {
        return baseSource.type();
    }
    
    @Override
    public BigInteger size() {
        return monotonic.size();
    }

    @Override
    public T get(BigInteger index) {
        return baseSource.get(monotonic.at(index));
    }

    @Override
    public Range find(Object value) {
        return monotonic.indicesOf(baseSource.find(value));
    }

    @Override
    public Range findBetween(
            Object minValue, boolean minInclusive, Object maxValue, boolean maxInclusive) {
        return monotonic.indicesOf(baseSource.findBetween(
                minValue, minInclusive, maxValue, maxInclusive));
    }
    
}
