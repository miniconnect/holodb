package hu.webarticum.holodb.data.distribution;

import java.math.BigInteger;

public interface Sampler {

    public BigInteger sample();
    
    public BigInteger size();
    
}
