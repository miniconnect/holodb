package hu.webarticum.holodb.core.data.binrel.monotonic;

import java.math.BigInteger;

import hu.webarticum.holodb.core.data.binrel.core.Function;
import hu.webarticum.holodb.core.data.selection.Range;

public interface Monotonic extends Function {

    public Range indicesOf(BigInteger value);

    public BigInteger imageSize();
    
}
