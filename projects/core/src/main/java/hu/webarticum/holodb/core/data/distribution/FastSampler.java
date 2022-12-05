package hu.webarticum.holodb.core.data.distribution;

import hu.webarticum.miniconnect.lang.LargeInteger;

public class FastSampler implements Sampler {

    private final LargeInteger size;

    private final LargeInteger sample;
    
    
    public FastSampler(LargeInteger size) {
        this.size = size;
        this.sample = size.divide(LargeInteger.of(2L));
    }
    
    
    @Override
    public LargeInteger sample() {
        return sample;
    }

    @Override
    public LargeInteger size() {
        return size;
    }

}
