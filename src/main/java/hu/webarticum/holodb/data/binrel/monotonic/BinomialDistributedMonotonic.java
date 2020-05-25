package hu.webarticum.holodb.data.binrel.monotonic;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.distribution.BinomialDistribution;

import hu.webarticum.holodb.data.random.TreeRandom;
import hu.webarticum.holodb.data.random.TreeRandomUtil;
import hu.webarticum.holodb.data.selection.Range;

public class BinomialDistributedMonotonic implements Monotonic {

    private final TreeRandom treeRandom;

    private final BigInteger size;
    
    private final BigInteger imageSize;
    
    
    private final Map<BigInteger, BigInteger> cachedSplitPoints = new HashMap<>();

    
    public BinomialDistributedMonotonic(TreeRandom treeRandom, long size, long imageSize) {
        this(treeRandom, BigInteger.valueOf(size), BigInteger.valueOf(imageSize));
    }
    
    public BinomialDistributedMonotonic(TreeRandom treeRandom, BigInteger size, BigInteger imageSize) {
        this.treeRandom = treeRandom;
        this.size = size;
        this.imageSize = imageSize;
    }
    
    
    @Override
    public BigInteger size() {
        return size;
    }

    @Override
    public BigInteger at(BigInteger index) {
        Range range = Range.fromLength(BigInteger.ZERO, size);
        Range imageRange = Range.fromLength(BigInteger.ZERO, imageSize);
        int level = 0;
        while (imageRange.getLength().compareTo(BigInteger.ONE) > 0) {
            BigInteger imageSplitPoint = imageRange.getFrom().add(imageRange.getUntil()).divide(BigInteger.TWO);
            BigInteger splitPoint = split(range, imageRange, imageSplitPoint, level);
            if (splitPoint.compareTo(index) > 0) {
                range = Range.fromUntil(range.getFrom(), splitPoint);
                imageRange = Range.fromUntil(imageRange.getFrom(), imageSplitPoint);
            } else {
                range = Range.fromUntil(splitPoint, range.getUntil());
                imageRange = Range.fromUntil(imageSplitPoint, imageRange.getUntil());
            }
            level++;
        }
        return imageRange.at(BigInteger.ZERO);
    }

    @Override
    public Range indicesOf(BigInteger value) {
        Range range = Range.fromLength(BigInteger.ZERO, size);
        Range imageRange = Range.fromLength(BigInteger.ZERO, imageSize);
        int level = 0;
        while (imageRange.getLength().compareTo(BigInteger.ONE) > 0) {
            BigInteger imageSplitPoint = imageRange.getFrom().add(imageRange.getUntil()).divide(BigInteger.TWO);
            BigInteger splitPoint = split(range, imageRange, imageSplitPoint, level);
            if (imageSplitPoint.compareTo(value) > 0) {
                range = Range.fromUntil(range.getFrom(), splitPoint);
                imageRange = Range.fromUntil(imageRange.getFrom(), imageSplitPoint);
            } else {
                range = Range.fromUntil(splitPoint, range.getUntil());
                imageRange = Range.fromUntil(imageSplitPoint, imageRange.getUntil());
            }
            level++;
        }
        return range;
    }
    
    private BigInteger split(Range range, Range imageRange, BigInteger imageSplitPoint, int level) {
        BigInteger length = range.getLength();
        
        BigInteger cachedSplitPoint = cachedSplitPoints.get(imageSplitPoint);
        if (cachedSplitPoint != null) {
            return cachedSplitPoint;
        }
        
        BigInteger splitPoint;
        if (length.compareTo(BigInteger.valueOf(100000L)) > 0) {
            splitPoint = splitFast(range, imageSplitPoint);
        } else if (length.equals(BigInteger.ZERO)) {
            splitPoint = range.getFrom();
        } else {
            splitPoint = splitBinomial(range, imageRange, imageSplitPoint);
        }
        
        if (level < 10) {
            cachedSplitPoints.put(imageSplitPoint, splitPoint);
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
        double probability = imageFirstLength.doubleValue() / imageRange.getLength().doubleValue();
        BinomialDistribution binomialDistribution = new BinomialDistribution(range.getLength().intValue(), probability);
        long seed = TreeRandomUtil.fetchLong(treeRandom.sub(imageSplitPoint));
        binomialDistribution.reseedRandomGenerator(seed);
        BigInteger relativeSplitPoint = BigInteger.valueOf(binomialDistribution.sample());
        return range.getFrom().add(relativeSplitPoint);
    }
    
    @Override
    public BigInteger imageSize() {
        return imageSize;
    }

}
