package hu.webarticum.holodb.core.util;

import java.math.BigInteger;

import org.apache.commons.math3.fraction.BigFraction;

public final class MathUtil {

    private MathUtil() {
    }

    
    public static double divideBigIntegers(BigInteger numerator, BigInteger denominator) {
        return new BigFraction(numerator, denominator).doubleValue();
    }

}
