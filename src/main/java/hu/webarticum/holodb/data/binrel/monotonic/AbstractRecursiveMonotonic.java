package hu.webarticum.holodb.data.binrel.monotonic;

import java.math.BigInteger;

import hu.webarticum.holodb.data.selection.Range;

public abstract class AbstractRecursiveMonotonic implements Monotonic {

    private final BigInteger size;
    
    private final BigInteger imageSize;

    
    protected AbstractRecursiveMonotonic(BigInteger size, BigInteger imageSize) {
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

    protected abstract BigInteger split(Range range, Range imageRange, BigInteger imageSplitPoint, int level);
}
