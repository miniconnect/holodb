package hu.webarticum.holodb.core.data.source;

import hu.webarticum.holodb.core.data.selection.Selection;

// TODO: make this similar-to/compatible-with TableIndex
public interface Index {

    public default Selection find(Object value) {
        return findBetween(value, true, value, true);
    }
    
    public Selection findBetween(
            Object minValue, boolean minInclusive, Object maxValue, boolean maxInclusive);
    
}
