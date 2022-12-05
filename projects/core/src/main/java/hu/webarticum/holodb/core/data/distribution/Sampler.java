package hu.webarticum.holodb.core.data.distribution;

import hu.webarticum.miniconnect.lang.LargeInteger;

public interface Sampler {

    public LargeInteger sample();
    
    public LargeInteger size();
    
}
