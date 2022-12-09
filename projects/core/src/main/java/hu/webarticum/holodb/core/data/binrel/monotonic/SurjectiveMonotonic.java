package hu.webarticum.holodb.core.data.binrel.monotonic;

import hu.webarticum.holodb.core.data.distribution.Sampler;
import hu.webarticum.holodb.core.data.random.TreeRandom;
import hu.webarticum.holodb.core.data.random.TreeRandomUtil;
import hu.webarticum.holodb.core.data.selection.Range;
import hu.webarticum.holodb.core.util.MathUtil;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class SurjectiveMonotonic extends AbstractCachingRecursiveMonotonic {

    private static final SamplerFactory DEFAULT_SAMPLER_FACTORY = SamplerFactory.DEFAULT;
    
    private static final int DEFAULT_CACHE_DEPTH = 10;
    
    private static final LargeInteger DEFAULT_SAMPLER_MAX_LENGTH = LargeInteger.of(1000L);
    
    
    private final TreeRandom treeRandom;
    
    private final SamplerFactory samplerFactory;
    
    private final LargeInteger samplerMaxLength;

    
    public SurjectiveMonotonic(TreeRandom treeRandom, long size, long imageSize) {
        this(treeRandom, LargeInteger.of(size), LargeInteger.of(imageSize));
    }

    public SurjectiveMonotonic(TreeRandom treeRandom, SamplerFactory samplerFactory, long size, long imageSize) {
        this(treeRandom, samplerFactory, LargeInteger.of(size), LargeInteger.of(imageSize), DEFAULT_CACHE_DEPTH);
    }

    public SurjectiveMonotonic(TreeRandom treeRandom, LargeInteger size, LargeInteger imageSize) {
        this(treeRandom, size, imageSize, DEFAULT_CACHE_DEPTH);
    }

    public SurjectiveMonotonic(
            TreeRandom treeRandom, LargeInteger size, LargeInteger imageSize, int cacheDepth) {
        this(treeRandom, DEFAULT_SAMPLER_FACTORY, size, imageSize, cacheDepth);
    }

    public SurjectiveMonotonic(
            TreeRandom treeRandom,
            SamplerFactory samplerFactory,
            LargeInteger size,
            LargeInteger imageSize,
            int cacheDepth) {
        this(treeRandom, samplerFactory, size, imageSize, cacheDepth, DEFAULT_SAMPLER_MAX_LENGTH);
    }
    
    public SurjectiveMonotonic(
            TreeRandom treeRandom, SamplerFactory samplerFactory,
            LargeInteger size, LargeInteger imageSize, int cacheDepth, LargeInteger samplerMaxLength) {
        
        super(checkSize(size, imageSize), imageSize, cacheDepth);
        this.treeRandom = treeRandom;
        this.samplerFactory = samplerFactory;
        this.samplerMaxLength = samplerMaxLength;
    }
    
    private static LargeInteger checkSize(LargeInteger size, LargeInteger imageSize) {
        if (size.isLessThan(imageSize)) {
            throw new IllegalArgumentException("size must not be less then imageSize");
        }
        return size;
    }
    
    
    @Override
    protected LargeInteger splitCacheable(Range range, Range imageRange, LargeInteger imageSplitPoint, int level) {
        LargeInteger length = range.size();
        LargeInteger splitPoint;
        Range rangeToSplit = Range.fromUntil(
                range.from().add(imageSplitPoint.subtract(imageRange.from())),
                range.until().subtract(imageRange.until().subtract(imageSplitPoint)));
        
        if (length.isGreaterThan(samplerMaxLength)) {
            splitPoint = splitFast(rangeToSplit);
        } else {
            splitPoint = splitWithSampler(rangeToSplit, imageRange, imageSplitPoint);
        }

        return splitPoint;
    }

    private LargeInteger splitFast(Range range) {
        return range.from().add(range.size().divide(LargeInteger.of(2L)));
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
