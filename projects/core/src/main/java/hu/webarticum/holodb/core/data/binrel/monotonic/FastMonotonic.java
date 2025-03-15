package hu.webarticum.holodb.core.data.binrel.monotonic;

import hu.webarticum.holodb.core.data.selection.Range;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class FastMonotonic implements Monotonic {

    
    private final LargeInteger size;
    
    private final LargeInteger imageSize;
    

    public FastMonotonic(long size, long imageSize) {
        this(LargeInteger.of(size), LargeInteger.of(imageSize));
    }
    
    public FastMonotonic(LargeInteger size, LargeInteger imageSize) {
        this.size = size;
        this.imageSize = imageSize;
    }
    
    
    @Override
    public LargeInteger size() {
        return size;
    }

    @Override
    public LargeInteger at(LargeInteger index) {
        return index.multiply(imageSize).divide(size);
    }

    @Override
    public Range indicesOf(LargeInteger value) {
        return Range.fromUntil(calculateFrom(value), calculateFrom(value.add(LargeInteger.ONE)));
    }

    @Override
    public Range indicesOf(Range range) {
        return Range.fromUntil(calculateFrom(range.from()), calculateFrom(range.until()));
    }
    
    private LargeInteger calculateFrom(LargeInteger value) {
        LargeInteger product = value.multiply(size);
        LargeInteger result = product.divide(imageSize);
        if (!product.mod(imageSize).equals(LargeInteger.ZERO)) {
            result = result.add(LargeInteger.ONE);
        }
        return result;
    }

    @Override
    public LargeInteger imageSize() {
        return imageSize;
    }

}
