package hu.webarticum.holodb.core.data.source;

import hu.webarticum.holodb.core.data.selection.Selection;

// TODO: make this similar-to/compatible-with TableIndex
// TODO: create a basic/general/abstract 'Searchable' (or so) interface in miniconnect
// TODO: make parameter non-generic (Object)
// FIXME: Selection vs. TableSelection
public interface Index<T> {

    public default Selection find(T value) {
        return findBetween(value, true, value, true);
    }
    
    public Selection findBetween(
            T minValue, boolean minInclusive,
            T maxValue, boolean maxInclusive);
    
}
