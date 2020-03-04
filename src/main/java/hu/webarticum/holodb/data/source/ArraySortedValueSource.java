package hu.webarticum.holodb.data.source;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import hu.webarticum.holodb.util.Range;

public class ArraySortedValueSource<T extends Comparable<T>> implements SortedValueSource<T> {

    private BigInteger length;
    
    private Object[] values;
    

    public ArraySortedValueSource(T[] values) {
        this(Arrays.asList(values));
    }
    
    public ArraySortedValueSource(Collection<T> values) {
        this(toSortedSet(values));
    }
    
    private ArraySortedValueSource(SortedSet<T> set) {
        int size = set.size();
        this.length = BigInteger.valueOf(size);
        this.values = new Object[size];
        int i = 0;
        for (T value : set) {
            this.values[i] = value;
            i++;
        }
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
    public BigInteger size() {
        return length;
    }

    @Override
    public T at(BigInteger index) {
        @SuppressWarnings("unchecked")
        T result = (T) values[index.intValue()];
        return result;
    }

    @Override
    public boolean isUnique() {
        return true;
    }

    @Override
    public Range find(T value) {
        int position = Arrays.binarySearch(values, value);
        return position >= 0 ? Range.fromLength(position, 1) : Range.fromLength(-1 - position, 0);
    }

    @Override
    public Range findBetween(T minValue, boolean minInclusive, T maxValue, boolean maxInclusive) {
        int cmp = minValue.compareTo(maxValue);
        if (cmp == 0) {
            if (!minInclusive && !maxInclusive) {
                throw new IllegalArgumentException("The single value can not be included and excluded at the same time");
            }
        } else if (cmp > 0) {
            throw new IllegalArgumentException("minValue can not be larger than maxValue");
        }
        
        Range minRange = find(minValue);
        BigInteger from = minInclusive ? minRange.getFrom() : minRange.getUntil();
        Range maxRange = find(maxValue);
        BigInteger until = maxInclusive ? maxRange.getUntil() : maxRange.getFrom();
        return Range.fromUntil(from, until);
    }

}
