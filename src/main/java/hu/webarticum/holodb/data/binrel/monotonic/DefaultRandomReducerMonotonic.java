package hu.webarticum.holodb.data.binrel.monotonic;

import java.math.BigInteger;

import hu.webarticum.holodb.data.random.TreeRandom;
import hu.webarticum.holodb.data.selection.Range;

public class DefaultRandomReducerMonotonic implements Monotonic {

    private final TreeRandom treeRandom;
    
    private final BigInteger size;
    
    private final BigInteger imageSize;
    
    
    public DefaultRandomReducerMonotonic(TreeRandom treeRandom, BigInteger size, BigInteger imageSize) {
        if (size.compareTo(imageSize) > 0) {
            throw new IllegalArgumentException("Size can not be larger than imageSize");
        }
        
        this.treeRandom = treeRandom;
        this.size = size;
        this.imageSize = imageSize;
    }
    
    
    @Override
    public BigInteger size() {
        return size;
    }
    
    @Override
    public BigInteger imageSize() {
        return imageSize;
    }
    
    @Override
    public boolean isReversible() {
        return true;
    }
    
    @Override
    public BigInteger at(BigInteger index) {
        BigInteger fromInclusive = getLowValue(index);
        BigInteger maximumExclusive = getLowValue(index.add(BigInteger.ONE));
        BigInteger highExclusive = maximumExclusive.subtract(fromInclusive);
        return treeRandom.sub(fromInclusive).getNumber(highExclusive).add(fromInclusive);
    }

    private BigInteger getLowValue(BigInteger index) {
        BigInteger product = index.multiply(imageSize);
        BigInteger remainder = product.mod(size);
        BigInteger value = product.divide(size);
        if (remainder.equals(BigInteger.ZERO)) {
            return value;
        } else {
            return value.add(BigInteger.ONE);
        }
    }
    
    @Override
    public Range indicesOf(BigInteger value) {
        BigInteger index = value.multiply(size).divide(imageSize);
        BigInteger valueAtIndex = at(index);
        if (valueAtIndex.equals(value)) {
            return Range.fromLength(index, 1);
        } else {
            return Range.fromLength(index, 0);
        }
    }
    
}
