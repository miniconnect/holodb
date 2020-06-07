package hu.webarticum.holodb.data.distribution;

import java.math.BigInteger;

public class FastSampler implements Sampler {

    private final BigInteger size;

    private final BigInteger sample;
    
    
    public FastSampler(BigInteger size) {
        this.size = size;
        this.sample = size.divide(BigInteger.TWO);
    }
    
    
    @Override
    public BigInteger sample() {
        return sample;
    }

    @Override
    public BigInteger size() {
        return size;
    }

}
