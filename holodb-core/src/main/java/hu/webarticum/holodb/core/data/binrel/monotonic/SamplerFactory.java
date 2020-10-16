package hu.webarticum.holodb.core.data.binrel.monotonic;

import java.math.BigInteger;

import hu.webarticum.holodb.core.data.distribution.ApacheCommonsBinomialSampler;
import hu.webarticum.holodb.core.data.distribution.Sampler;

public interface SamplerFactory {
    
    public static final SamplerFactory DEFAULT = (seed, size, probability) ->
            new ApacheCommonsBinomialSampler(seed, size.intValue(), probability);
    
    
    public Sampler create(long seed, BigInteger size, double probability);

}