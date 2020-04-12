package hu.webarticum.holodb.data.binrel.monotonic;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

public abstract class AbstractMonotonicTest<T extends Monotonic> {

    protected abstract T create(BigInteger size, BigInteger imageSize);

    
    @Test
    void testXXX() {
        
        // TODO
        
    }
    
}
