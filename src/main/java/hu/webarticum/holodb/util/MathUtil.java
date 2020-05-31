package hu.webarticum.holodb.util;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.apache.commons.math3.fraction.BigFraction;

public final class MathUtil {

    private MathUtil() {
    }

    
    public static double divideBigIntegers(BigInteger numerator, BigInteger denominator) {
        return new BigFraction(numerator, denominator).doubleValue();
    }

    // FIXME
    @SuppressWarnings("deprecation")
    public static BigInteger scaleBigInteger(BigInteger number, double scale) {
        return new BigFraction(number).multiply(new BigFraction(scale))
                .bigDecimalValue(0, BigDecimal.ROUND_DOWN).toBigInteger(); // NOSONAR
    }
    
}
