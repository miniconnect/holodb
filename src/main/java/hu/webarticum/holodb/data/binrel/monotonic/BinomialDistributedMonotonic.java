package hu.webarticum.holodb.data.binrel.monotonic;

import java.math.BigInteger;

import org.apache.commons.math3.distribution.BinomialDistribution;

import hu.webarticum.holodb.data.random.TreeRandom;
import hu.webarticum.holodb.data.random.TreeRandomUtil;
import hu.webarticum.holodb.data.selection.Range;

public class BinomialDistributedMonotonic implements Monotonic {

    private final TreeRandom treeRandom;

    private final BigInteger size;
    
    private final BigInteger imageSize;

    
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
        while (imageRange.getLength().compareTo(BigInteger.ONE) > 0) {
            BigInteger imageSplitPoint = imageRange.getFrom().add(imageRange.getUntil()).divide(BigInteger.TWO);
            BigInteger splitPoint = split(range, imageRange, imageSplitPoint);
            if (splitPoint.compareTo(index) > 0) {
                range = Range.fromUntil(range.getFrom(), splitPoint);
                imageRange = Range.fromUntil(imageRange.getFrom(), imageSplitPoint);
            } else {
                range = Range.fromUntil(splitPoint, range.getUntil());
                imageRange = Range.fromUntil(imageSplitPoint, imageRange.getUntil());
            }
        }
        return imageRange.at(BigInteger.ZERO);
    }

    @Override
    public Range indicesOf(BigInteger value) {
        
        // TODO
        
        return null;
    }
    
    private BigInteger split(Range range, Range imageRange, BigInteger imageSplitPoint) {
        BigInteger length = range.getLength();
        
        if (length.compareTo(BigInteger.valueOf(100000L)) > 0) {
            return range.getFrom().add(length.divide(BigInteger.TWO));
        }
        
        BigInteger imageFirstLength = imageSplitPoint.subtract(imageRange.getFrom());
        double probability = imageFirstLength.doubleValue() / imageRange.getLength().doubleValue();
        BinomialDistribution binomialDistribution = new BinomialDistribution(length.intValue(), probability);
        long seed = TreeRandomUtil.getLong(treeRandom.sub(range.getFrom()).sub(range.getLength()));
        binomialDistribution.reseedRandomGenerator(seed);
        BigInteger relativeSplitPoint = BigInteger.valueOf(binomialDistribution.sample());
        return range.getFrom().add(relativeSplitPoint);
        
    }
    
    @Override
    public BigInteger imageSize() {
        return imageSize;
    }

}
