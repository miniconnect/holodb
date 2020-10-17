package hu.webarticum.holodb.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class ObjectUtilTest {

    @Test
    void testApply() {
        AtomicInteger counter = new AtomicInteger(77);
        AtomicInteger counterAfter = ObjectUtil.apply(counter, c -> c.addAndGet(12));
        assertThat(counter).isSameAs(counterAfter);
        assertThat(counterAfter.intValue()).isEqualTo(89);
    }
    
}
