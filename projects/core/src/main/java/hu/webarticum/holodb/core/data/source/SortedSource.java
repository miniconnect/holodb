package hu.webarticum.holodb.core.data.source;

import hu.webarticum.holodb.core.data.selection.Range;

public interface SortedSource<T> extends IndexedSource<T> {

    @Override
    public Range find(Object value);

    @Override
    public Range findBetween(
            Object minValue, boolean minInclusive, Object maxValue, boolean maxInclusive);

    @Override
    public Range findNulls();

}
