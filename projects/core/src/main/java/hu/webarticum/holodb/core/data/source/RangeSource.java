package hu.webarticum.holodb.core.data.source;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.IntStream;

import hu.webarticum.holodb.core.data.selection.Range;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class RangeSource implements SortedSource<LargeInteger> {

    private static final int MAX_COUNT_OF_POSSIBLE_VALUES = 1000;


    private final LargeInteger from;

    private final LargeInteger size;


    public RangeSource(LargeInteger size) {
        this(LargeInteger.ZERO, size);
    }

    public RangeSource(LargeInteger from, LargeInteger size) {
        this.from = from;
        this.size = size;
    }


    @Override
    public Class<LargeInteger> type() {
        return LargeInteger.class;
    }

    @Override
    public LargeInteger size() {
        return size;
    }

    @Override
    public LargeInteger get(LargeInteger index) {
        return from.add(index);
    }

    @Override
    public Comparator<LargeInteger> comparator() {
        return LargeInteger::compareTo;
    }

    @Override
    public Optional<ImmutableList<LargeInteger>> possibleValues() {
        if (size.isGreaterThan(LargeInteger.of(MAX_COUNT_OF_POSSIBLE_VALUES))) {
            return Optional.empty();
        }

        ImmutableList<LargeInteger> valueList = IntStream
                .range(0, size.intValueExact())
                .mapToObj(i -> get(LargeInteger.of(i)))
                .collect(ImmutableList.createCollector());
        return Optional.of(valueList);
    }

    @Override
    public Range find(Object value) {
        return findBetween(value, true, value, true);
    }

    @Override
    public Range findBetween(Object minValue, boolean minInclusive, Object maxValue, boolean maxInclusive) {
        LargeInteger queryFrom = fromOf(minValue, minInclusive);
        LargeInteger queryUntil = untilOf(maxValue, maxInclusive);
        return Range.fromUntil(queryFrom.subtract(from), queryUntil.subtract(from));
    }

    private LargeInteger fromOf(Object minValue, boolean minInclusive) {
        if (minValue == null) {
            return from;
        }

        LargeInteger until = from.add(size);
        LargeInteger queryMin = (LargeInteger) minValue;
        LargeInteger queryFrom = minInclusive ? queryMin : queryMin.add(LargeInteger.ONE);
        return keepBetween(queryFrom, from, until);
    }

    private LargeInteger untilOf(Object maxValue, boolean maxInclusive) {
        LargeInteger until = from.add(size);

        if (maxValue == null) {
            return until;
        }

        LargeInteger queryMax = (LargeInteger) maxValue;
        LargeInteger queryUntil = maxInclusive ? queryMax.add(LargeInteger.ONE) : queryMax;
        return keepBetween(queryUntil, from, until);
    }

    private LargeInteger keepBetween(LargeInteger value, LargeInteger min, LargeInteger max) {
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
