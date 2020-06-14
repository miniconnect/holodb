package hu.webarticum.holodb.core.data.binrel.monotonic;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import hu.webarticum.holodb.core.data.binrel.monotonic.Monotonic;
import hu.webarticum.holodb.core.data.selection.Range;

public abstract class AbstractMonotonicTest<T extends Monotonic> {

    protected abstract T create(BigInteger size, BigInteger imageSize);

    
    @Test
    void testEmpty() {
        checkSize(create(BigInteger.ZERO, BigInteger.ZERO), BigInteger.ZERO, BigInteger.ZERO);
    }

    @Test
    void testSmallInstancesCompletely() {
        for (long i = 7; i <= 50; i += 3) {
            for (long j = 4; j <= 50; j += 3) {
                BigInteger size = BigInteger.valueOf(i);
                BigInteger imageSize = BigInteger.valueOf(j);
                Monotonic monotonic = create(size, imageSize);
                checkSize(monotonic, size, imageSize);
                checkMonotonic(monotonic);
            }
        }
    }
    
    @Test
    void testLargeInstancesPartially() {
        BigInteger from = new BigInteger("87495827938452954757483254");
        BigInteger until = new BigInteger("123252453566234501434504213");
        BigInteger step = new BigInteger("6344583456194523561031234");
        for (BigInteger size = from; size.compareTo(until) < 0; size = size.add(step)) {
            for (BigInteger imageSize = from; imageSize.compareTo(until) < 0; imageSize = imageSize.add(step)) {
                Monotonic monotonic = create(size, imageSize);
                checkSize(monotonic, size, imageSize);
                checkProbablyMonotonic(monotonic);
            }
        }
    }

    private void checkSize(Monotonic monotonic, BigInteger size, BigInteger imageSize) {
        assertThat(monotonic).extracting(Monotonic::size).as("monotonic size").isEqualTo(size);
        assertThat(monotonic).extracting(Monotonic::imageSize).as("monotonic image size").isEqualTo(imageSize);
    }
    
    private void checkMonotonic(Monotonic monotonic) {
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
