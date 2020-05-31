package hu.webarticum.holodb.data.binrel.monotonic;

import java.math.BigInteger;

import org.apache.commons.math3.util.FastMath;

import hu.webarticum.holodb.data.random.TreeRandom;
import hu.webarticum.holodb.data.random.TreeRandomUtil;
import hu.webarticum.holodb.data.selection.Range;
import hu.webarticum.holodb.util.MathUtil;

// TODO: review, optimize, rename
public class TestMonotonic extends AbstractCachingRecursiveMonotonic {

    private static final int DEFAULT_CACHE_DEPTH = 10;
    
    
    private final TreeRandom treeRandom;

    
    public TestMonotonic(TreeRandom treeRandom, long size, long imageSize) {
        this(treeRandom, BigInteger.valueOf(size), BigInteger.valueOf(imageSize));
    }

    public TestMonotonic(TreeRandom treeRandom, BigInteger size, BigInteger imageSize) {
        this(treeRandom, size, imageSize, DEFAULT_CACHE_DEPTH);
    }
    
    public TestMonotonic(TreeRandom treeRandom, BigInteger size, BigInteger imageSize, int cacheDepth) {
        super(size, imageSize, cacheDepth);
        this.treeRandom = treeRandom;
    }

    
    @Override
    protected BigInteger splitCacheable(Range range, Range imageRange, BigInteger imageSplitPoint, int level) {
        if (range.isEmpty()) {
            return range.getFrom();
        }

        BigInteger imageFirstLength = imageSplitPoint.subtract(imageRange.getFrom());
        double mode = MathUtil.divideBigIntegers(imageFirstLength, imageRange.getLength());
        double at = TreeRandomUtil.fetchSmallDouble(treeRandom.sub(imageSplitPoint));
        double sample = getFastSample(mode, at);

        BigInteger relativeSplitPoint = MathUtil.scaleBigInteger(range.getLength(), sample);
        return range.getFrom().add(relativeSplitPoint);
    }

    private double getFastSample(double mode, double at) {
        double transformed = (at - 0.5) * 2;
        double powered = FastMath.pow(transformed, ((size().bitLength() / 2) * 2) + 1L); // NOSONAR
        double multiplier = (at < 0.5) ? mode : (1 - mode);
        return (multiplier * powered) + mode;
    }
    
}
