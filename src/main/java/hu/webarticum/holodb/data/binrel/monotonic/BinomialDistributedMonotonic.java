package hu.webarticum.holodb.data.binrel.monotonic;

import java.math.BigInteger;

import org.apache.commons.math3.distribution.BinomialDistribution;

import hu.webarticum.holodb.data.random.TreeRandom;
import hu.webarticum.holodb.data.random.TreeRandomUtil;
import hu.webarticum.holodb.data.selection.Range;
import hu.webarticum.holodb.util.MathUtil;

public class BinomialDistributedMonotonic extends AbstractCachingRecursiveMonotonic {

    private static final int DEFAULT_CACHE_DEPTH = 10;
    
    private static final BigInteger BINOMIAL_MAX_LENGTH = BigInteger.valueOf(1000L);
    
    
    private final TreeRandom treeRandom;

    
    public BinomialDistributedMonotonic(TreeRandom treeRandom, long size, long imageSize) {
        this(treeRandom, BigInteger.valueOf(size), BigInteger.valueOf(imageSize));
    }

    public BinomialDistributedMonotonic(TreeRandom treeRandom, BigInteger size, BigInteger imageSize) {
        this(treeRandom, size, imageSize, DEFAULT_CACHE_DEPTH);
    }

    public BinomialDistributedMonotonic(TreeRandom treeRandom, BigInteger size, BigInteger imageSize, int cacheDepth) {
        super(size, imageSize, cacheDepth);
        this.treeRandom = treeRandom;
    }
    
    
    @Override
    protected BigInteger splitCacheable(Range range, Range imageRange, BigInteger imageSplitPoint, int level) {
        BigInteger length = range.getLength();
        
        BigInteger splitPoint;
        if (length.compareTo(BINOMIAL_MAX_LENGTH) > 0) {
            splitPoint = splitFast(range, imageSplitPoint);
        } else if (length.equals(BigInteger.ZERO)) {
            splitPoint = range.getFrom();
        } else {
            splitPoint = splitBinomial(range, imageRange, imageSplitPoint);
        }
        
        return splitPoint;
    }

    private BigInteger splitFast(Range range, BigInteger imageSplitPoint) {
        BigInteger rangeLength = BigInteger.TEN;
        BigInteger rangeSplitPoint = treeRandom.sub(imageSplitPoint).getNumber(rangeLength);
        BigInteger relativeFixedPoint = range.getLength().divide(BigInteger.TWO);
        BigInteger relativeSplitPoint = relativeFixedPoint.subtract(rangeLength.divide(BigInteger.TWO)).add(rangeSplitPoint);
        return range.getFrom().add(relativeSplitPoint);
    }

    private BigInteger splitBinomial(Range range, Range imageRange, BigInteger imageSplitPoint) {
        BigInteger imageFirstLength = imageSplitPoint.subtract(imageRange.getFrom());
        double probability = MathUtil.divideBigIntegers(imageFirstLength, imageRange.getLength());
        
        // we use splitBinomial() only for smaller ranges (<= BINOMIAL_MAX_LENGTH), int is suitable to store their length
        int rangeIntLength = range.getLength().intValue();
        
        BinomialDistribution binomialDistribution = new BinomialDistribution(rangeIntLength, probability);
        long seed = TreeRandomUtil.fetchLong(treeRandom.sub(imageSplitPoint));
        binomialDistribution.reseedRandomGenerator(seed);
        BigInteger relativeSplitPoint = BigInteger.valueOf(binomialDistribution.sample());
        return range.getFrom().add(relativeSplitPoint);
    }
    
}
