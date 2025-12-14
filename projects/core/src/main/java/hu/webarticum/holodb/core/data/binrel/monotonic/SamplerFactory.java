package hu.webarticum.holodb.core.data.binrel.monotonic;

import hu.webarticum.holodb.core.data.distribution.ApacheCommonsBinomialSampler;
import hu.webarticum.holodb.core.data.distribution.Sampler;
import hu.webarticum.miniconnect.lang.LargeInteger;

public interface SamplerFactory {

    // FIXME: functional lambda?
    // TODO: SamplerFactory::isFast()
    // TODO: SamplerFactory::isBig()


    public static final SamplerFactory DEFAULT = (seed, size, probability) ->
            new ApacheCommonsBinomialSampler(seed, size.intValue(), probability);


    public Sampler create(long seed, LargeInteger size, double probability);

}
