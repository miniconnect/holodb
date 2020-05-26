package hu.webarticum.holodb.data.binrel.monotonic;

import java.math.BigInteger;

import hu.webarticum.holodb.data.selection.Range;

public class FastMonotonic implements Monotonic {

    
    private final BigInteger size;
    
    private final BigInteger imageSize;
    

    public FastMonotonic(long size, long imageSize) {
        this(BigInteger.valueOf(size), BigInteger.valueOf(imageSize));
    }
    
    public FastMonotonic(BigInteger size, BigInteger imageSize) {
        this.size = size;
        this.imageSize = imageSize;
    }
    
    
    @Override
    public BigInteger size() {
        return size;
    }

    @Override
    public BigInteger at(BigInteger index) {
        return index.multiply(imageSize).divide(size);
    }

    @Override
    public Range indicesOf(BigInteger value) {
        return Range.fromUntil(calculateFrom(value), calculateFrom(value.add(BigInteger.ONE)));
    }
    
    private BigInteger calculateFrom(BigInteger value) {
        BigInteger product = value.multiply(size);
        BigInteger result = product.divide(imageSize);
        if (!product.mod(imageSize).equals(BigInteger.ZERO)) {
            result = result.add(BigInteger.ONE);
        }
        return result;
    }

    @Override
    public BigInteger imageSize() {
        return imageSize;
    }

}
