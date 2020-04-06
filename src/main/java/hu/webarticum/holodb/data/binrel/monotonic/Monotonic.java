package hu.webarticum.holodb.data.binrel.monotonic;

import java.math.BigInteger;

import hu.webarticum.holodb.data.binrel.core.Function;
import hu.webarticum.holodb.data.selection.Range;

public interface Monotonic extends Function {

    public Range indicesOf(BigInteger value);

    public BigInteger imageSize(); // FIXME name?
    
}
