package hu.webarticum.holodb.core.data.source;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;

import hu.webarticum.holodb.core.data.selection.Range;
import hu.webarticum.miniconnect.lang.ImmutableList;

public class TransformingSortedSource<T, U> implements SortedSource<U> {

    private final SortedSource<T> baseSource;

    private final Class<U> type;
    
    private final Function<U, T> encoder;
    
    private final Function<T, U> decoder;
    
    private final Comparator<?> comparator;
    
    
    public TransformingSortedSource(
            SortedSource<T> baseSource,
            Class<U> type,
            Function<U, T> encoder,
            Function<T, U> decoder) {
        this.baseSource = baseSource;
        this.type = type;
        this.encoder = encoder;
        this.decoder = decoder;
        this.comparator = createComparator(baseSource, encoder);
    }
    
    private static <T, U> Comparator<T> createComparator(IndexedSource<U> baseSource, Function<T, U> encoder) {
        @SuppressWarnings("unchecked")
        Comparator<U> baseComparator = (Comparator<U>) baseSource.comparator();
        return (a, b) -> baseComparator.compare(encoder.apply(a), encoder.apply(b));
    }
    
    
    @Override
    public Comparator<?> comparator() {
        return comparator;
    }

    @Override
    public Range find(Object value) {
        @SuppressWarnings("unchecked")
        Object encodedValue = value != null ? encoder.apply((U) value) : null;
        return baseSource.find(encodedValue);
    }
    
    @Override
    public Range findBetween(Object minValue, boolean minInclusive, Object maxValue, boolean maxInclusive) {
        @SuppressWarnings("unchecked")
        Object encodedMinValue = minValue != null ? encoder.apply((U) minValue) : null;
        @SuppressWarnings("unchecked")
        Object encodedMaxValue = maxValue != null ? encoder.apply((U) maxValue) : null;
        return baseSource.findBetween(encodedMinValue, minInclusive, encodedMaxValue, maxInclusive);
    }

    @Override
    public Range findNulls() {
        return baseSource.findNulls();
    }

    @Override
    public Class<U> type() {
        return type;
    }

    @Override
    public BigInteger size() {
        return baseSource.size();
    }

    @Override
    public U get(BigInteger index) {
        T encodedValue = baseSource.get(index);
        return encodedValue != null ? decoder.apply(encodedValue) : null;
    }

    @Override
    public Optional<ImmutableList<U>> possibleValues() {
        Optional<ImmutableList<T>> encodedPossibleValues = baseSource.possibleValues();
        return encodedPossibleValues.isPresent() ?
                Optional.of(encodedPossibleValues.get().map(decoder)) :
                Optional.empty();
    }

}
