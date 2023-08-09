package hu.webarticum.holodb.core.data.distribution;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import hu.webarticum.miniconnect.lang.LargeInteger;

abstract class AbstractSamplerTest<T extends Sampler> {

    protected abstract T create(LargeInteger size);

    
    @Test
    void testEmpty() {
        Sampler sampler = create(LargeInteger.ZERO);
        assertThat(sampler.size()).isEqualTo(LargeInteger.ZERO);
        assertThat(sampler.sample()).isEqualTo(LargeInteger.ZERO);
    }

    @Test
    void testSmall() {
        LargeInteger size = LargeInteger.TEN;
        Sampler sampler = create(size);
        assertThat(sampler.size()).isEqualTo(size);
        assertThat(sampler.sample()).isGreaterThanOrEqualTo(LargeInteger.ZERO).isLessThanOrEqualTo(size);
    }
    
}
