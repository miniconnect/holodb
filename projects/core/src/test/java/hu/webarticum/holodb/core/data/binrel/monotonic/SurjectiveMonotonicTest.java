package hu.webarticum.holodb.core.data.binrel.monotonic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigInteger;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import hu.webarticum.holodb.core.data.random.HasherTreeRandom;
import hu.webarticum.holodb.core.data.random.TreeRandom;
import hu.webarticum.holodb.core.data.selection.Range;

class SurjectiveMonotonicTest extends AbstractMonotonicTest<SurjectiveMonotonic> {

    private TreeRandom treeRandom = new HasherTreeRandom();
    
    
    @Override
    protected SurjectiveMonotonic create(BigInteger size, BigInteger imageSize) {
        return new SurjectiveMonotonic(treeRandom, size, imageSize);
    }
    
    @Override
    protected boolean isNarrowingEnabled() {
        return false;
    }
    

    @Test
    void testInputConstraint() {
        assertThatThrownBy(() -> new SurjectiveMonotonic( // NOSONAR
                treeRandom, BigInteger.valueOf(15), BigInteger.valueOf(25)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testSurjectivity() {
        checkSurjective(create(BigInteger.valueOf(15), BigInteger.valueOf(10)));
        checkSurjective(create(BigInteger.valueOf(643), BigInteger.valueOf(234)));
        checkSurjective(create(BigInteger.valueOf(643), BigInteger.valueOf(15)));
    }
    
    public void checkSurjective(SurjectiveMonotonic monotonic) {
        Set<BigInteger> values = new LinkedHashSet<BigInteger>();
        for (BigInteger value : monotonic) {
            values.add(value);
        }
        assertThat(values).containsExactlyElementsOf(
                Range.until(monotonic.imageSize()));
    }
    
}
