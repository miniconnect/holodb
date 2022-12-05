package hu.webarticum.holodb.core.data.binrel.monotonic;

import hu.webarticum.holodb.core.data.selection.Range;
import hu.webarticum.miniconnect.lang.LargeInteger;

public abstract class AbstractRecursiveMonotonic implements Monotonic {

    private final LargeInteger size;
    
    private final LargeInteger imageSize;

    
    protected AbstractRecursiveMonotonic(LargeInteger size, LargeInteger imageSize) {
        this.size = size;
        this.imageSize = imageSize;
    }
    
    
    @Override
    public LargeInteger size() {
        return size;
    }

    @Override
    public LargeInteger imageSize() {
        return imageSize;
    }

    @Override
    public LargeInteger at(LargeInteger index) {
        Range range = Range.fromSize(LargeInteger.ZERO, size);
        Range imageRange = Range.fromSize(LargeInteger.ZERO, imageSize);
        int level = 0;
        while (imageRange.size().compareTo(LargeInteger.ONE) > 0) {
            LargeInteger imageSplitPoint = imageRange.from().add(imageRange.until()).divide(LargeInteger.of(2L));
            LargeInteger splitPoint = split(range, imageRange, imageSplitPoint, level);
            if (splitPoint.compareTo(index) > 0) {
                range = Range.fromUntil(range.from(), splitPoint);
                imageRange = Range.fromUntil(imageRange.from(), imageSplitPoint);
            } else {
                range = Range.fromUntil(splitPoint, range.until());
                imageRange = Range.fromUntil(imageSplitPoint, imageRange.until());
            }
            level++;
        }
        return imageRange.at(LargeInteger.ZERO);
    }

    @Override
    public Range indicesOf(LargeInteger value) {
        Range range = Range.fromSize(LargeInteger.ZERO, size);
        Range imageRange = Range.fromSize(LargeInteger.ZERO, imageSize);
        int level = 0;
        while (imageRange.size().compareTo(LargeInteger.ONE) > 0) {
            LargeInteger imageSplitPoint = imageRange.from().add(imageRange.until()).divide(LargeInteger.of(2L));
            LargeInteger splitPoint = split(range, imageRange, imageSplitPoint, level);
            if (imageSplitPoint.compareTo(value) > 0) {
                range = Range.fromUntil(range.from(), splitPoint);
                imageRange = Range.fromUntil(imageRange.from(), imageSplitPoint);
            } else {
                range = Range.fromUntil(splitPoint, range.until());
                imageRange = Range.fromUntil(imageSplitPoint, imageRange.until());
            }
            level++;
        }
        return range;
    }

    protected abstract LargeInteger split(Range range, Range imageRange, LargeInteger imageSplitPoint, int level);
    
}
