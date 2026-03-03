package hu.webarticum.holodb.core.data.source;

import java.util.Comparator;
import java.util.Optional;

import hu.webarticum.holodb.core.data.selection.Range;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class NullPaddedSortedSource<T> implements SortedSource<T> {

    private final SortedSource<T> baseSource;

    private final LargeInteger size;


    public NullPaddedSortedSource(SortedSource<T> baseSource, LargeInteger size) {
        if (baseSource.size().isGreaterThan(size)) {
            throw new IllegalArgumentException("Base source size can not be larger than target size");
        }

        this.baseSource = baseSource;
        this.size = size;
    }


    @Override
    public Class<?> type() {
        return baseSource.type();
    }

    @Override
    public LargeInteger size() {
        return size;
    }

    @Override
    public T get(LargeInteger index) {
        return index.isLessThan(baseSource.size()) ? baseSource.get(index) : null;
    }

    @Override
    public Comparator<?> comparator() {
        return baseSource.comparator();
    }

    @Override
    public Optional<ImmutableList<T>> possibleValues() {
        return baseSource.possibleValues();
    }

    @Override
    public Range find(Object value) {
        return baseSource.find(value);
    }

    @Override
    public Range findBetween(Object minValue, boolean minInclusive, Object maxValue, boolean maxInclusive) {
        return baseSource.findBetween(minValue, minInclusive, maxValue, maxInclusive);
    }

    @Override
    public Range findNulls() {
        return Range.fromUntil(baseSource.size(), size);
    }

}
