package hu.webarticum.holodb.core.data.binrel.monotonic;

import hu.webarticum.holodb.core.data.distribution.Sampler;
import hu.webarticum.holodb.core.data.random.TreeRandom;
import hu.webarticum.holodb.core.data.random.TreeRandomUtil;
import hu.webarticum.holodb.core.data.selection.Range;
import hu.webarticum.holodb.core.util.MathUtil;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class BinomialMonotonic extends AbstractCachingRecursiveMonotonic {

    private static final SamplerFactory DEFAULT_SAMPLER_FACTORY = SamplerFactory.DEFAULT;
    
    private static final int DEFAULT_CACHE_DEPTH = 10;
    
    private static final LargeInteger DEFAULT_SAMPLER_MAX_LENGTH = LargeInteger.of(1000L);
    
    
    private final TreeRandom treeRandom;
    
    private final SamplerFactory samplerFactory;
    
    private final LargeInteger samplerMaxLength;

    
    public BinomialMonotonic(TreeRandom treeRandom, long size, long imageSize) {
        this(treeRandom, LargeInteger.of(size), LargeInteger.of(imageSize));
    }

    public BinomialMonotonic(TreeRandom treeRandom, SamplerFactory samplerFactory, long size, long imageSize) {
        this(treeRandom, samplerFactory, LargeInteger.of(size), LargeInteger.of(imageSize), DEFAULT_CACHE_DEPTH);
    }

    public BinomialMonotonic(TreeRandom treeRandom, LargeInteger size, LargeInteger imageSize) {
        this(treeRandom, size, imageSize, DEFAULT_CACHE_DEPTH);
    }

    public BinomialMonotonic(TreeRandom treeRandom, LargeInteger size, LargeInteger imageSize, int cacheDepth) {
        this(treeRandom, DEFAULT_SAMPLER_FACTORY, size, imageSize, cacheDepth);
    }

    public BinomialMonotonic(
            TreeRandom treeRandom, SamplerFactory samplerFactory, LargeInteger size, LargeInteger imageSize, int cacheDepth) {
        this(treeRandom, samplerFactory, size, imageSize, cacheDepth, DEFAULT_SAMPLER_MAX_LENGTH);
    }
    
    public BinomialMonotonic(
            TreeRandom treeRandom, SamplerFactory samplerFactory,
            LargeInteger size, LargeInteger imageSize, int cacheDepth, LargeInteger samplerMaxLength) {
        
        super(size, imageSize, cacheDepth);
        this.treeRandom = treeRandom;
        this.samplerFactory = samplerFactory;
        this.samplerMaxLength = samplerMaxLength;
    }
    
    
    @Override
    protected LargeInteger splitCacheable(Range range, Range imageRange, LargeInteger imageSplitPoint, int level) {
        LargeInteger length = range.size();
        LargeInteger splitPoint;
        if (length.isGreaterThan(samplerMaxLength)) {
            splitPoint = splitFast(range, imageSplitPoint);
        } else if (length.equals(LargeInteger.ZERO)) {
            splitPoint = range.from();
        } else {
            splitPoint = splitWithSampler(range, imageRange, imageSplitPoint);
        }
        
        return splitPoint;
    }

    private LargeInteger splitFast(Range range, LargeInteger imageSplitPoint) {
        LargeInteger rangeLength = LargeInteger.TEN;
        LargeInteger rangeSplitPoint = treeRandom.sub(imageSplitPoint).getNumber(rangeLength);
        LargeInteger relativeFixedPoint = range.size().divide(LargeInteger.of(2L));
        LargeInteger relativeSplitPoint =
                relativeFixedPoint.subtract(rangeLength.divide(LargeInteger.of(2L))).add(rangeSplitPoint);
        return range.from().add(relativeSplitPoint);
    }

    private LargeInteger splitWithSampler(Range range, Range imageRange, LargeInteger imageSplitPoint) {
        LargeInteger imageFirstLength = imageSplitPoint.subtract(imageRange.from());
        double probability = MathUtil.divideLargeIntegers(imageFirstLength, imageRange.size());
        long seed = TreeRandomUtil.fetchLong(treeRandom.sub(imageSplitPoint));
        Sampler sampler = samplerFactory.create(seed, range.size(), probability);
        LargeInteger relativeSplitPoint = sampler.sample();
        return range.from().add(relativeSplitPoint);
    }
    

}
