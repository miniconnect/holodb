package hu.webarticum.holodb.core.data.binrel.monotonic;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import hu.webarticum.holodb.core.data.selection.Range;

abstract class AbstractMonotonicTest<T extends Monotonic> {

    protected abstract T create(BigInteger size, BigInteger imageSize);

    protected boolean isNarrowingEnabled() {
        return true;
    }
    
    
    @Test
    void testEmpty() {
        checkSize(create(BigInteger.ZERO, BigInteger.ZERO), BigInteger.ZERO, BigInteger.ZERO);
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
                BigInteger imageSize = BigInteger.valueOf(i);
                BigInteger size = BigInteger.valueOf(j);
                Monotonic monotonic = create(size, imageSize);
                checkSize(monotonic, size, imageSize);
                checkMonotonic(monotonic);
            }
        }
    }
    
    @Test
    void testLargeInstancesPartially() {
        BigInteger innerTo = new BigInteger("123252453566234501434504213");
        BigInteger outerFrom = new BigInteger("87495827938452954757483254");
        BigInteger outerTo = isNarrowingEnabled() ?
                innerTo :
                new BigInteger("102729083749083816548294783");
        BigInteger step = new BigInteger("6344583456194523561031234");
        for (BigInteger imageSize = outerFrom; imageSize.compareTo(outerTo) <= 0; imageSize = imageSize.add(step)) {
            BigInteger innerFrom = isNarrowingEnabled() ? outerFrom : imageSize;
            for (BigInteger size = innerFrom; size.compareTo(innerTo) <= 0; size = size.add(step)) {
                Monotonic monotonic = create(size, imageSize);
                checkSize(monotonic, size, imageSize);
                checkProbablyMonotonic(monotonic);
            }
        }
    }

    private void checkSize(Monotonic monotonic, BigInteger size, BigInteger imageSize) {
        assertThat(monotonic.size()).as("monotonic size").isEqualTo(size);
        assertThat(monotonic.imageSize()).as("monotonic image size").isEqualTo(imageSize);
    }

    private void checkMonotonic(Monotonic monotonic) {
        checkValueRanges(monotonic);
        checkRangeRanges(monotonic);
    }
    
    private void checkValueRanges(Monotonic monotonic) {
        BigInteger size = monotonic.size();
        BigInteger imageSize = monotonic.imageSize();
        
        Map<BigInteger, Range> rangeMap1 = new HashMap<>();
        BigInteger previousValue = BigInteger.valueOf(-1);
        BigInteger previousSplitPoint = null;
        BigInteger currentValue = null;
        for (BigInteger index = BigInteger.ZERO; index.compareTo(size) < 0; index = index.add(BigInteger.ONE)) {
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

        Map<BigInteger, Range> rangeMap2 = new HashMap<>();
        for (BigInteger value = BigInteger.ZERO; value.compareTo(imageSize) < 0; value = value.add(BigInteger.ONE)) {
            Range indexRange = monotonic.indicesOf(value);
            if (!indexRange.isEmpty()) {
                rangeMap2.put(value, indexRange);
            }
        }

        assertThat(rangeMap1).as("ranges from full scan compared to queried index ranges").isEqualTo(rangeMap2);
    }

    private void checkRangeRanges(Monotonic monotonic) {
        Range fromEmptyActual = monotonic.indicesOf(Range.empty(BigInteger.ZERO));
        assertThat(fromEmptyActual).isEqualTo(Range.empty(BigInteger.ZERO));

        BigInteger imageSize = monotonic.imageSize();
        BigInteger size = monotonic.size();
        
        if (imageSize.signum() == 1) {
            Range untilEmptyActual = monotonic.indicesOf(Range.empty(imageSize));
            assertThat(untilEmptyActual).isEqualTo(Range.empty(size));
            
            Range fromUntilActual = monotonic.indicesOf(Range.fromUntil(BigInteger.ZERO, imageSize));
            assertThat(fromUntilActual).isEqualTo(Range.fromUntil(BigInteger.ZERO, size));
            
            if (imageSize.compareTo(BigInteger.ONE) > 0) {
                BigInteger midValue = imageSize.divide(BigInteger.TWO);
                BigInteger midIndex = monotonic.indicesOf(midValue).from();
                Range untilActual = monotonic.indicesOf(Range.fromUntil(BigInteger.ZERO, midValue));
                Range untilExpected = Range.fromUntil(BigInteger.ZERO, midIndex);
                assertThat(untilActual).isEqualTo(untilExpected);

                Range midEmptyActual = monotonic.indicesOf(Range.fromSize(midValue, BigInteger.ZERO));
                Range midEmptyExpected = Range.fromSize(midIndex, BigInteger.ZERO);
                assertThat(midEmptyActual).isEqualTo(midEmptyExpected);

                if (imageSize.compareTo(BigInteger.TWO) > 0) {
                    Range firstRange = monotonic.indicesOf(Range.fromSize(BigInteger.ZERO, BigInteger.ONE));
                    BigInteger lastValue = imageSize.subtract(BigInteger.ONE);
                    Range lastRange = monotonic.indicesOf(Range.fromSize(lastValue, BigInteger.ONE));

                    Range midActual = monotonic.indicesOf(Range.fromUntil(BigInteger.ONE, lastValue));
                    Range midExpected = Range.fromUntil(firstRange.until(), lastRange.from());
                    assertThat(midActual).isEqualTo(midExpected);
                }
            }
        }
    }

    private void checkProbablyMonotonic(Monotonic monotonic) {
        BigInteger size = monotonic.size();
        BigInteger imageSize = monotonic.imageSize();
        
        BigInteger step = size.divide(BigInteger.valueOf(20));

        BigInteger previousFetchedValue = BigInteger.valueOf(-1);
        for (BigInteger index = BigInteger.ZERO; index.compareTo(size) < 0; index = index.add(step)) {
            BigInteger value = monotonic.at(index);
            assertThat(value).as("value compared to previous fetched").isGreaterThanOrEqualTo(previousFetchedValue);
            assertThat(value).as("value in image").isLessThan(imageSize);
            Range valueIndexRange = monotonic.indicesOf(value);
            assertThat(valueIndexRange.contains(index)).as("range contains index").isTrue();
        }
    }

}
