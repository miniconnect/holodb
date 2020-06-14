package hu.webarticum.holodb.core.data.binrel.core;

import java.math.BigInteger;

public interface Function {

    public BigInteger size();
    
    public BigInteger at(BigInteger index);
    
}
