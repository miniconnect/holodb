package hu.webarticum.holodb.core.data.source;

import hu.webarticum.holodb.core.data.selection.Selection;

public interface Index<T> {

    public default Selection find(T value) {
        return findBetween(value, true, value, true);
    }
    
    public Selection findBetween(
            T minValue, boolean minInclusive,
            T maxValue, boolean maxInclusive);
    
}
