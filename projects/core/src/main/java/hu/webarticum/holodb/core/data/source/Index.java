package hu.webarticum.holodb.core.data.source;

import hu.webarticum.holodb.core.data.selection.Selection;

public interface Index {

    public Selection findBetween(
            Object minValue, boolean minInclusive, Object maxValue, boolean maxInclusive);

    public Selection findNulls();
    
    
    public default Selection find(Object value) {
        return findBetween(value, true, value, true);
    }
    
}
