package hu.webarticum.holodb.core.data.source;

import hu.webarticum.holodb.core.data.selection.Selection;

public interface IndexedSource<T> extends Index<T>, Source<T> {

    public Selection find(T value);

    public Selection findBetween(
            T minValue, boolean minInclusive,
            T maxValue, boolean maxInclusive);
    
}
