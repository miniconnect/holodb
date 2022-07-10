package hu.webarticum.holodb.core.data.source;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.IntStream;

import hu.webarticum.holodb.core.data.selection.Range;
import hu.webarticum.miniconnect.lang.ImmutableList;

public class RangeSource implements SortedSource<BigInteger> {
    
    private static final int MAX_COUNT_OF_POSSIBLE_VALUES = 1000;
    
    
    private final BigInteger from;
    
    private final BigInteger size;
    

    public RangeSource(BigInteger size) {
        this(BigInteger.ZERO, size);
    }
    
    public RangeSource(BigInteger from, BigInteger size) {
        this.from = from;
        this.size = size;
    }
    

    @Override
    public Class<BigInteger> type() {
        return BigInteger.class;
    }

    @Override
    public BigInteger size() {
        return size;
    }

    @Override
    public BigInteger get(BigInteger index) {
        return from.add(index);
    }
    
    @Override
    public Comparator<BigInteger> comparator() {
        return BigInteger::compareTo;
    }

    @Override
    public Optional<ImmutableList<BigInteger>> possibleValues() {
        if (size.compareTo(BigInteger.valueOf(MAX_COUNT_OF_POSSIBLE_VALUES)) > 0) {
            return Optional.empty();
        }
        
        ImmutableList<BigInteger> valueList = IntStream
                .range(0, size.intValueExact())
                .mapToObj(i -> get(BigInteger.valueOf(i)))
                .collect(ImmutableList.createCollector());
        return Optional.of(valueList);
    }

    @Override
    public Range find(Object value) {
        return findBetween(value, true, value, true);
    }

    @Override
    public Range findBetween(
            Object minValue, boolean minInclusive, Object maxValue, boolean maxInclusive) {
        BigInteger until = from.add(size);
        
        BigInteger queryMin = (BigInteger) minValue;
        BigInteger queryFrom = minInclusive ? queryMin : queryMin.add(BigInteger.ONE);
        queryFrom = keepBetween(queryFrom, from, until);
        
        BigInteger queryMax = (BigInteger) maxValue;
        BigInteger queryUntil = maxInclusive ? queryMax.add(BigInteger.ONE) : queryMax;
        queryUntil = keepBetween(queryUntil, from, until);
        
        return Range.fromUntil(queryFrom.subtract(from), queryUntil.subtract(from));
    }
    
    private BigInteger keepBetween(BigInteger value, BigInteger min, BigInteger max) {
        if (value.compareTo(min) < 0) {
            return min;
        } else if (value.compareTo(max) > 0) {
            return max;
        } else {
            return value;
        }
    }

    @Override
    public Range findNulls() {
        return Range.empty();
    }
    
}
