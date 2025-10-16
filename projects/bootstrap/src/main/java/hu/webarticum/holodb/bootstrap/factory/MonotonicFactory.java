package hu.webarticum.holodb.bootstrap.factory;

import hu.webarticum.holodb.config.HoloConfigColumn.DistributionQuality;
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
        if (distributionQuality == DistributionQuality.HIGH) {
            return new BinomialMonotonic(treeRandom, size, baseSize);
        } else {
            return new FastMonotonic(size, baseSize);
        }
    }
    
}
