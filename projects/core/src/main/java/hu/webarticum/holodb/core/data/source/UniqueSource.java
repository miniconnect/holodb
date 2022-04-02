package hu.webarticum.holodb.core.data.source;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import hu.webarticum.holodb.core.data.selection.Range;

public class UniqueSource<T extends Comparable<T>> implements SortedSource<T> {

    private Class<T> type;

    private BigInteger length;
    
    private Object[] values;
    

    @SuppressWarnings("unchecked")
    public UniqueSource(T... values) {
        this((Class<T>) values.getClass().getComponentType(), Arrays.asList(values));
    }
    
    public UniqueSource(Class<T> type, Collection<T> values) {
        this(type, toSortedSet(values));
    }
    
    private UniqueSource(Class<T> type, SortedSet<T> set) {
        this.type = type;
        this.length = BigInteger.valueOf(set.size());
        this.values = set.toArray();
    }
    
    private static <T> SortedSet<T> toSortedSet(Collection<T> values) {
        if (values instanceof SortedSet) {
            SortedSet<T> castedSet = (SortedSet<T>) values;
            if (castedSet.comparator() == null) {
                return castedSet;
            }
        }
        
        return new TreeSet<>(values);
    }
    
    
    @Override
    public Class<T> type() {
        return type;
    }
    
    @Override
    public BigInteger size() {
        return length;
    }

    @Override
    public T get(BigInteger index) {
        @SuppressWarnings("unchecked")
        T result = (T) values[index.intValue()];
        return result;
    }

    @Override
    public Range find(T value) {
        int position = Arrays.binarySearch(values, value);
        return position >= 0 ? Range.fromSize(position, 1) : Range.fromSize((-1 - position), 0);
    }

    @Override
    public Range findBetween(T minValue, boolean minInclusive, T maxValue, boolean maxInclusive) {
        if (minValue != null && maxValue != null) {
            int cmp = minValue.compareTo(maxValue);
            if (cmp > 0 || (cmp == 0 && !minInclusive && !maxInclusive)) {
                return Range.empty(find(minValue).from());
            }
        }
        
        BigInteger from;
        if (minValue != null) {
            Range minRange = find(minValue);
            from = minInclusive ? minRange.from() : minRange.until();
        } else {
            from = BigInteger.ZERO;
        }
        
        BigInteger until;
        if (maxValue != null) {
            Range maxRange = find(maxValue);
            until = maxInclusive ? maxRange.until() : maxRange.from();
        } else {
            until = BigInteger.valueOf(values.length);
        }
        
        return Range.fromUntil(from, until);
    }

}
