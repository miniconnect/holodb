package hu.webarticum.holodb.app.factory;

import hu.webarticum.holodb.app.config.HoloConfigColumn.DistributionQuality;
import hu.webarticum.holodb.core.data.binrel.monotonic.BinomialMonotonic;
import hu.webarticum.holodb.core.data.binrel.monotonic.FastMonotonic;
import hu.webarticum.holodb.core.data.binrel.monotonic.Monotonic;
import hu.webarticum.holodb.core.data.random.TreeRandom;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class MonotonicFactory {
    
    private MonotonicFactory() {
        // static class
    }
    

    public static Monotonic createMonotonic(
            TreeRandom treeRandom, LargeInteger size, LargeInteger baseSize, DistributionQuality distributionQuality) {
        if (distributionQuality == DistributionQuality.MEDIUM) {
            // TODO: can we increase performance for MEDIUM?
            return new BinomialMonotonic(treeRandom, size, baseSize);
        } else if (distributionQuality == DistributionQuality.LOW) {
            return new FastMonotonic(size, baseSize);
        } else if (distributionQuality == DistributionQuality.HIGH) {
            return new BinomialMonotonic(treeRandom, size, baseSize);
        } else {
            throw new IllegalArgumentException("Unknown distributionQuality: " + distributionQuality);
        }
    }
    
}
