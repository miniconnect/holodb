package hu.webarticum.holodb.core.data.source;

import hu.webarticum.holodb.core.data.selection.Range;

public interface SortedSource<T> extends IndexedSource<T> {

    @Override
    public Range find(T value);

    @Override
    public Range findBetween(T minValue, boolean minInclusive, T maxValue, boolean maxInclusive);
    
}
