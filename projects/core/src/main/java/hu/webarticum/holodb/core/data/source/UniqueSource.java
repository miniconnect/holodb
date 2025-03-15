package hu.webarticum.holodb.core.data.source;

import java.text.Collator;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

import hu.webarticum.holodb.core.data.selection.Range;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class UniqueSource<T extends Comparable<T>> implements SortedSource<T> {

    private final Class<T> type;

    private final LargeInteger length;

    private final Comparator<T> comparator;
    
    private final Object[] values;
    

    @SuppressWarnings("unchecked")
    public UniqueSource(T... values) {
        this((Class<T>) values.getClass().getComponentType(), Arrays.asList(values));
    }
    
    public UniqueSource(Class<T> type, Collection<T> values) {
        this(type, values, null);
    }

    public UniqueSource(Class<T> type, Collection<T> values, Comparator<T> comparator) {
        values.forEach(Objects::requireNonNull);
        
        this.type = type;
        this.comparator = comparator != null ? comparator : createDefaultComparatorFor(type);
        this.values = toValueArray(values, this.comparator);
        this.length = LargeInteger.of(this.values.length);
    }

    @SuppressWarnings("unchecked")
    private static <T> Comparator<T> createDefaultComparatorFor(Class<T> clazz) {
        if (clazz == String.class) {
            return (Comparator<T>) Collator.getInstance(Locale.US);
        } else {
            return (Comparator<T>) Comparator.naturalOrder();
        }
    }
    
    private static <T> Object[] toValueArray(Collection<T> values, Comparator<T> comparator) {
        SortedSet<T> set = new TreeSet<>(comparator);
        set.addAll(values);
        return set.toArray();
    }
    
    
    @Override
    public Class<T> type() {
        return type;
    }
    
    @Override
    public LargeInteger size() {
        return length;
    }

    @Override
    public T get(LargeInteger index) {
        @SuppressWarnings("unchecked")
        T result = (T) values[index.intValue()];
        return result;
    }
    
    @Override
    public Comparator<T> comparator() {
        return comparator;
    }

    @Override
    public Optional<ImmutableList<T>> possibleValues() {
        @SuppressWarnings("unchecked")
        ImmutableList<T> valueList = (ImmutableList<T>) (ImmutableList<?>) ImmutableList.of(values);
        return Optional.of(valueList);
    }
    
    @Override
    public Range find(Object value) {
        @SuppressWarnings("unchecked")
        Comparator<Object> objectComparator = (Comparator<Object>) (Comparator<?>) comparator;
        int position = Arrays.binarySearch(values, value, objectComparator);
        return position >= 0 ? Range.fromSize(position, 1) : Range.fromSize((-1 - position), 0);
    }

    @Override
    public Range findBetween(
            Object minValue, boolean minInclusive, Object maxValue, boolean maxInclusive) {
        if (minValue != null && maxValue != null) {
            @SuppressWarnings("unchecked")
            int cmp = comparator.compare((T) minValue, (T) maxValue);
            if (cmp > 0 || (cmp == 0 && !minInclusive && !maxInclusive)) {
                return Range.empty(find(minValue).from());
            }
        }
        
        LargeInteger from;
        if (minValue != null) {
            Range minRange = find(minValue);
            from = minInclusive ? minRange.from() : minRange.until();
        } else {
            from = LargeInteger.ZERO;
        }
        
        LargeInteger until;
        if (maxValue != null) {
            Range maxRange = find(maxValue);
            until = maxInclusive ? maxRange.until() : maxRange.from();
        } else {
            until = length;
        }
        
        return Range.fromUntil(from, until);
    }

    @Override
    public Range findNulls() {
        return Range.empty();
    }
    
}
