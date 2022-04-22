package hu.webarticum.holodb.core.data.binrel.monotonic;

import java.math.BigInteger;

import hu.webarticum.holodb.core.data.distribution.Sampler;
import hu.webarticum.holodb.core.data.random.TreeRandom;
import hu.webarticum.holodb.core.data.random.TreeRandomUtil;
import hu.webarticum.holodb.core.data.selection.Range;
import hu.webarticum.holodb.core.util.MathUtil;

public class BinomialMonotonic extends AbstractCachingRecursiveMonotonic {

    private static final SamplerFactory DEFAULT_SAMPLER_FACTORY = SamplerFactory.DEFAULT;
    
    private static final int DEFAULT_CACHE_DEPTH = 10;
    
    private static final BigInteger DEFAULT_SAMPLER_MAX_LENGTH = BigInteger.valueOf(1000L);
    
    
    private final TreeRandom treeRandom;
    
    private final SamplerFactory samplerFactory;
    
    private final BigInteger samplerMaxLength;

    
    public BinomialMonotonic(TreeRandom treeRandom, long size, long imageSize) {
        this(treeRandom, BigInteger.valueOf(size), BigInteger.valueOf(imageSize));
    }

    public BinomialMonotonic(TreeRandom treeRandom, SamplerFactory samplerFactory, long size, long imageSize) {
        this(treeRandom, samplerFactory, BigInteger.valueOf(size), BigInteger.valueOf(imageSize), DEFAULT_CACHE_DEPTH);
    }

    public BinomialMonotonic(TreeRandom treeRandom, BigInteger size, BigInteger imageSize) {
        this(treeRandom, size, imageSize, DEFAULT_CACHE_DEPTH);
    }

    public BinomialMonotonic(TreeRandom treeRandom, BigInteger size, BigInteger imageSize, int cacheDepth) {
        this(treeRandom, DEFAULT_SAMPLER_FACTORY, size, imageSize, cacheDepth);
    }

    public BinomialMonotonic(
            TreeRandom treeRandom, SamplerFactory samplerFactory, BigInteger size, BigInteger imageSize, int cacheDepth) {
        this(treeRandom, samplerFactory, size, imageSize, cacheDepth, DEFAULT_SAMPLER_MAX_LENGTH);
    }
    
    public BinomialMonotonic(
            TreeRandom treeRandom, SamplerFactory samplerFactory,
            BigInteger size, BigInteger imageSize, int cacheDepth, BigInteger samplerMaxLength) {
        
        super(size, imageSize, cacheDepth);
        this.treeRandom = treeRandom;
        this.samplerFactory = samplerFactory;
        this.samplerMaxLength = samplerMaxLength;
    }
    
    
    @Override
    protected BigInteger splitCacheable(Range range, Range imageRange, BigInteger imageSplitPoint, int level) {
        BigInteger length = range.size();
        BigInteger splitPoint;
        if (length.compareTo(samplerMaxLength) > 0) {
            splitPoint = splitFast(range, imageSplitPoint);
        } else if (length.equals(BigInteger.ZERO)) {
            splitPoint = range.from();
        } else {
            splitPoint = splitWithSampler(range, imageRange, imageSplitPoint);
        }
        
        return splitPoint;
    }

    private BigInteger splitFast(Range range, BigInteger imageSplitPoint) {
        BigInteger rangeLength = BigInteger.TEN;
        BigInteger rangeSplitPoint = treeRandom.sub(imageSplitPoint).getNumber(rangeLength);
        BigInteger relativeFixedPoint = range.size().divide(BigInteger.valueOf(2L));
        BigInteger relativeSplitPoint = relativeFixedPoint.subtract(rangeLength.divide(BigInteger.valueOf(2L))).add(rangeSplitPoint);
        return range.from().add(relativeSplitPoint);
    }

    private BigInteger splitWithSampler(Range range, Range imageRange, BigInteger imageSplitPoint) {
        BigInteger imageFirstLength = imageSplitPoint.subtract(imageRange.from());
        double probability = MathUtil.divideBigIntegers(imageFirstLength, imageRange.size());
        long seed = TreeRandomUtil.fetchLong(treeRandom.sub(imageSplitPoint));
        Sampler sampler = samplerFactory.create(seed, range.size(), probability);
        BigInteger relativeSplitPoint = sampler.sample();
        return range.from().add(relativeSplitPoint);
    }
    

}
