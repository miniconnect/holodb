package hu.webarticum.holodb.core.util;

import org.apache.commons.math3.fraction.BigFraction;

import hu.webarticum.miniconnect.lang.LargeInteger;

public final class MathUtil {

    private MathUtil() {
    }

    
    public static double divideLargeIntegers(LargeInteger numerator, LargeInteger denominator) {
        return new BigFraction(numerator.bigIntegerValue(), denominator.bigIntegerValue()).doubleValue();
    }

}
