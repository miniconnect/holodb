package hu.webarticum.holodb.core.data.source;

import java.util.Comparator;

import hu.webarticum.holodb.core.data.selection.Selection;

public interface Index {

    public Comparator<?> comparator(); // NOSONAR it is unbound

    public Selection findBetween(
            Object minValue, boolean minInclusive, Object maxValue, boolean maxInclusive);

    public Selection findNulls();


    public default Selection find(Object value) {
        return findBetween(value, true, value, true);
    }

}
