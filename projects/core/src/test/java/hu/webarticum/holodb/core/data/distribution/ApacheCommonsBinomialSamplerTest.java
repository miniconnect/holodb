package hu.webarticum.holodb.core.data.distribution;

import hu.webarticum.miniconnect.lang.LargeInteger;

class ApacheCommonsBinomialSamplerTest extends AbstractSamplerTest<ApacheCommonsBinomialSampler> {

    private static final long SEED = 1234L;

    private static final double PROBABILITY = 0.5;

    @Override
    protected ApacheCommonsBinomialSampler create(LargeInteger size) {
        return new ApacheCommonsBinomialSampler(SEED, size.intValue(), PROBABILITY);
    }

}
