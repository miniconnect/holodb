package hu.webarticum.holodb.core.data.binrel.monotonic;

import hu.webarticum.holodb.core.data.binrel.Function;
import hu.webarticum.holodb.core.data.selection.Range;
import hu.webarticum.miniconnect.lang.LargeInteger;

public interface Monotonic extends Function {

    public Range indicesOf(LargeInteger value);

    public LargeInteger imageSize();


    public default Range indicesOf(Range range) {
        LargeInteger length = range.size();
        if (length.equals(LargeInteger.ZERO)) {
            LargeInteger from = range.from();
            if (from.equals(LargeInteger.ZERO)) {
                return Range.empty(LargeInteger.ZERO);
            } else if (from.equals(imageSize())) {
                return Range.empty(size());
            } else {
                return Range.empty(indicesOf(from).from());
            }
        } else if (length.equals(LargeInteger.ONE)) {
            return indicesOf(range.at(LargeInteger.ZERO));
        } else {
            Range fromRange = indicesOf(range.from());
            Range lastRange = indicesOf(range.until().subtract(LargeInteger.ONE));
            return Range.fromUntil(fromRange.from(), lastRange.until());
        }
    }

}
