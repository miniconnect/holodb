package hu.webarticum.holodb.core.data.binrel.monotonic;

import java.math.BigInteger;

import hu.webarticum.holodb.core.data.binrel.Function;
import hu.webarticum.holodb.core.data.selection.Range;

public interface Monotonic extends Function {

    public Range indicesOf(BigInteger value);

    public BigInteger imageSize();
    

    public default Range indicesOf(Range range) {
        BigInteger length = range.size();
        if (length.equals(BigInteger.ZERO)) {
            BigInteger from = range.from();
            if (from.equals(BigInteger.ZERO)) {
                return Range.empty(BigInteger.ZERO);
            } else if (from.equals(imageSize())) {
                return Range.empty(size());
            } else {
                return Range.empty(indicesOf(from).from());
            }
        } else if (length.equals(BigInteger.ONE)) {
            return indicesOf(range.at(BigInteger.ZERO));
        } else {
            Range fromRange = indicesOf(range.from());
            Range lastRange = indicesOf(range.until().subtract(BigInteger.ONE));
            return Range.fromUntil(fromRange.from(), lastRange.until());
        }
    }
    
}
