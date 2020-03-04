package hu.webarticum.holodb.data.source;

import hu.webarticum.holodb.util.Range;

public interface SortedValueSource<T> extends ValueSource<T> {

    public boolean isUnique();

    public Range find(T value);

    public Range findBetween(T minValue, boolean minInclusive, T maxValue, boolean maxInclusive);
    
}
