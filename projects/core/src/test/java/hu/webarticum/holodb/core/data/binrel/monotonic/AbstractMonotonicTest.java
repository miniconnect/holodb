package hu.webarticum.holodb.core.data.binrel.monotonic;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import hu.webarticum.holodb.core.data.selection.Range;
import hu.webarticum.miniconnect.lang.LargeInteger;

abstract class AbstractMonotonicTest<T extends Monotonic> {

    protected abstract T create(LargeInteger size, LargeInteger imageSize);

    protected boolean isNarrowingEnabled() {
        return true;
    }
    
    
    @Test
    void testEmpty() {
        checkSize(create(LargeInteger.ZERO, LargeInteger.ZERO), LargeInteger.ZERO, LargeInteger.ZERO);
    }

    @Test
    void testSmallInstancesCompletely() {
        long innerTo = 50;
        long outerFrom = 4;
        long outerTo = isNarrowingEnabled() ? innerTo : 40;
        long step = 3;
        for (long i = outerFrom; i < outerTo; i += step) {
            long innerFrom = isNarrowingEnabled() ? outerFrom : i;
            for (long j = innerFrom; j < innerTo; j += step) {
                LargeInteger imageSize = LargeInteger.of(i);
                LargeInteger size = LargeInteger.of(j);
                Monotonic monotonic = create(size, imageSize);
                checkSize(monotonic, size, imageSize);
                checkMonotonic(monotonic);
            }
        }
    }
    
    @Test
    void testLargeInstancesPartially() {
        LargeInteger innerTo = LargeInteger.of("123252453566234501434504213");
        LargeInteger outerFrom = LargeInteger.of("87495827938452954757483254");
        LargeInteger outerTo = isNarrowingEnabled() ? innerTo : LargeInteger.of("102729083749083816548294783");
        LargeInteger step = LargeInteger.of("6344583456194523561031234");
        for (LargeInteger imageSize = outerFrom; imageSize.compareTo(outerTo) <= 0; imageSize = imageSize.add(step)) {
            LargeInteger innerFrom = isNarrowingEnabled() ? outerFrom : imageSize;
            for (LargeInteger size = innerFrom; size.compareTo(innerTo) <= 0; size = size.add(step)) {
                Monotonic monotonic = create(size, imageSize);
                checkSize(monotonic, size, imageSize);
                checkProbablyMonotonic(monotonic);
            }
        }
    }

    private void checkSize(Monotonic monotonic, LargeInteger size, LargeInteger imageSize) {
        assertThat(monotonic.size()).as("monotonic size").isEqualTo(size);
        assertThat(monotonic.imageSize()).as("monotonic image size").isEqualTo(imageSize);
    }

    private void checkMonotonic(Monotonic monotonic) {
        checkValueRanges(monotonic);
        checkRangeRanges(monotonic);
    }
    
    private void checkValueRanges(Monotonic monotonic) {
        LargeInteger size = monotonic.size();
        LargeInteger imageSize = monotonic.imageSize();
        
        Map<LargeInteger, Range> rangeMap1 = new HashMap<>();
        LargeInteger previousValue = LargeInteger.of(-1);
        LargeInteger previousSplitPoint = null;
        LargeInteger currentValue = null;
        for (LargeInteger index = LargeInteger.ZERO; index.compareTo(size) < 0; index = index.add(LargeInteger.ONE)) {
            currentValue = monotonic.at(index);
            if (!currentValue.equals(previousValue)) {
                assertThat(currentValue).as("value compared to previous").isGreaterThanOrEqualTo(previousValue);
                assertThat(currentValue).as("value in image").isLessThan(imageSize);
                if (previousSplitPoint != null) {
                    rangeMap1.put(previousValue, Range.fromUntil(previousSplitPoint, index));
                }
                previousValue = currentValue;
                previousSplitPoint = index;
            }
        }
        if (previousSplitPoint.compareTo(size) < 0) {
            rangeMap1.put(currentValue, Range.fromUntil(previousSplitPoint, size));
        }

        Map<LargeInteger, Range> rangeMap2 = new HashMap<>();
        for (LargeInteger value = LargeInteger.ZERO; value.compareTo(imageSize) < 0; value = value.add(LargeInteger.ONE)) {
            Range indexRange = monotonic.indicesOf(value);
            if (!indexRange.isEmpty()) {
                rangeMap2.put(value, indexRange);
            }
        }

        assertThat(rangeMap1).as("ranges from full scan compared to queried index ranges").isEqualTo(rangeMap2);
    }

    private void checkRangeRanges(Monotonic monotonic) {
        Range fromEmptyActual = monotonic.indicesOf(Range.empty(LargeInteger.ZERO));
        assertThat(fromEmptyActual).isEqualTo(Range.empty(LargeInteger.ZERO));

        LargeInteger imageSize = monotonic.imageSize();
        LargeInteger size = monotonic.size();
        
        if (imageSize.signum() == 1) {
            Range untilEmptyActual = monotonic.indicesOf(Range.empty(imageSize));
            assertThat(untilEmptyActual).isEqualTo(Range.empty(size));
            
            Range fromUntilActual = monotonic.indicesOf(Range.fromUntil(LargeInteger.ZERO, imageSize));
            assertThat(fromUntilActual).isEqualTo(Range.fromUntil(LargeInteger.ZERO, size));
            
            if (imageSize.compareTo(LargeInteger.ONE) > 0) {
                LargeInteger midValue = imageSize.divide(LargeInteger.of(2L));
                LargeInteger midIndex = monotonic.indicesOf(midValue).from();
                Range untilActual = monotonic.indicesOf(Range.fromUntil(LargeInteger.ZERO, midValue));
                Range untilExpected = Range.fromUntil(LargeInteger.ZERO, midIndex);
                assertThat(untilActual).isEqualTo(untilExpected);

                Range midEmptyActual = monotonic.indicesOf(Range.fromSize(midValue, LargeInteger.ZERO));
                Range midEmptyExpected = Range.fromSize(midIndex, LargeInteger.ZERO);
                assertThat(midEmptyActual).isEqualTo(midEmptyExpected);

                if (imageSize.compareTo(LargeInteger.of(2L)) > 0) {
                    Range firstRange = monotonic.indicesOf(Range.fromSize(LargeInteger.ZERO, LargeInteger.ONE));
                    LargeInteger lastValue = imageSize.subtract(LargeInteger.ONE);
                    Range lastRange = monotonic.indicesOf(Range.fromSize(lastValue, LargeInteger.ONE));

                    Range midActual = monotonic.indicesOf(Range.fromUntil(LargeInteger.ONE, lastValue));
                    Range midExpected = Range.fromUntil(firstRange.until(), lastRange.from());
                    assertThat(midActual).isEqualTo(midExpected);
                }
            }
        }
    }

    private void checkProbablyMonotonic(Monotonic monotonic) {
        LargeInteger size = monotonic.size();
        LargeInteger imageSize = monotonic.imageSize();
        
        LargeInteger step = size.divide(LargeInteger.of(20));

        LargeInteger previousFetchedValue = LargeInteger.of(-1);
        for (LargeInteger index = LargeInteger.ZERO; index.compareTo(size) < 0; index = index.add(step)) {
            LargeInteger value = monotonic.at(index);
            assertThat(value).as("value compared to previous fetched").isGreaterThanOrEqualTo(previousFetchedValue);
            assertThat(value).as("value in image").isLessThan(imageSize);
            Range valueIndexRange = monotonic.indicesOf(value);
            assertThat(valueIndexRange.contains(index)).as("range contains index").isTrue();
        }
    }

}
