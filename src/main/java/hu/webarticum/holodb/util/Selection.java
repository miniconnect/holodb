package hu.webarticum.holodb.util;

import java.math.BigInteger;

public interface Selection {

    public BigInteger getCount();

    public boolean isEmpty();

    public BigInteger at(BigInteger index);
    
}
