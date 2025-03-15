package hu.webarticum.holodb.core.data.distribution;

import hu.webarticum.miniconnect.lang.LargeInteger;

class FastSamplerTest extends AbstractSamplerTest<FastSampler> {

    @Override
    protected FastSampler create(LargeInteger size) {
        return new FastSampler(size);
    }

}
