package hu.webarticum.holodb.core.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.Test;

import hu.webarticum.miniconnect.lang.LargeInteger;

class MathUtilTest {

    @Test
    void testDivideLargeIntegers() {
        assertThat(
                MathUtil.divideLargeIntegers(LargeInteger.ZERO, LargeInteger.of("273642342345572364734")))
                .isEqualTo(0d);
        assertThat(
                MathUtil.divideLargeIntegers(LargeInteger.of(1000), LargeInteger.of(2000)))
                .isCloseTo(0.5, Percentage.withPercentage(0.000001d));
        assertThat(
                MathUtil.divideLargeIntegers(LargeInteger.of("123456789123456789"), LargeInteger.of("2736472364734")))
                .isCloseTo(45115.3d, Percentage.withPercentage(0.0001d));
    }

    @Test
    void testDivideLargeIntegersThrowsException() {
        assertThatThrownBy(() -> MathUtil.divideLargeIntegers(LargeInteger.of(123), LargeInteger.ZERO))
                .isInstanceOf(RuntimeException.class);
    }

}
