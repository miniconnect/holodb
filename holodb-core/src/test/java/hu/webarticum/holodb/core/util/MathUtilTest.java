package hu.webarticum.holodb.core.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigInteger;

import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.Test;

public class MathUtilTest {

    @Test
    void testDivideBigIntegers() {
        assertThat(MathUtil.divideBigIntegers(BigInteger.ZERO, new BigInteger("273642342345572364734")))
                .isEqualTo(0d);
        assertThat(MathUtil.divideBigIntegers(BigInteger.valueOf(1000), BigInteger.valueOf(2000)))
                .isCloseTo(0.5, Percentage.withPercentage(0.000001d));
        assertThat(MathUtil.divideBigIntegers(new BigInteger("123456789123456789"), new BigInteger("2736472364734")))
                .isCloseTo(45115.3d, Percentage.withPercentage(0.0001d));
    }

    @Test
    void testDivideBigIntegersThrowsException() {
        assertThatThrownBy(() -> MathUtil.divideBigIntegers(BigInteger.valueOf(123), BigInteger.ZERO))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void testScaleBigInteger() {
        assertThat(MathUtil.scaleBigInteger(BigInteger.valueOf(1000), 2d)).isEqualTo(BigInteger.valueOf(2000));
        assertThat(MathUtil.scaleBigInteger(BigInteger.valueOf(77), 0.4d)).isEqualTo(BigInteger.valueOf(30));
        assertThat(MathUtil.scaleBigInteger(new BigInteger("23642834762384"), 2.35)).isEqualTo(new BigInteger("55560661691602"));
    }
    
}
