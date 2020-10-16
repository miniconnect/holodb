package hu.webarticum.holodb.core.data.selection;

import java.math.BigInteger;

public interface Selection {

    public BigInteger size();

    public boolean isEmpty();

    public BigInteger at(BigInteger index);
    
    public boolean contains(BigInteger value);
    
}
