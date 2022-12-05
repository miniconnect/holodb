package hu.webarticum.holodb.core.data.binrel.monotonic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import hu.webarticum.holodb.core.data.random.HasherTreeRandom;
import hu.webarticum.holodb.core.data.random.TreeRandom;
import hu.webarticum.holodb.core.data.selection.Range;
import hu.webarticum.miniconnect.lang.LargeInteger;

class SurjectiveMonotonicTest extends AbstractMonotonicTest<SurjectiveMonotonic> {

    private TreeRandom treeRandom = new HasherTreeRandom();
    
    
    @Override
    protected SurjectiveMonotonic create(LargeInteger size, LargeInteger imageSize) {
        return new SurjectiveMonotonic(treeRandom, size, imageSize);
    }
    
    @Override
    protected boolean isNarrowingEnabled() {
        return false;
    }
    

    @Test
    void testInputConstraint() {
        assertThatThrownBy(() -> new SurjectiveMonotonic( // NOSONAR
                treeRandom, LargeInteger.of(15), LargeInteger.of(25)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testSurjectivity() {
        checkSurjective(create(LargeInteger.of(15), LargeInteger.of(10)));
        checkSurjective(create(LargeInteger.of(643), LargeInteger.of(234)));
        checkSurjective(create(LargeInteger.of(643), LargeInteger.of(15)));
    }
    
    public void checkSurjective(SurjectiveMonotonic monotonic) {
        Set<LargeInteger> values = new LinkedHashSet<LargeInteger>();
        for (LargeInteger value : monotonic) {
            values.add(value);
        }
        assertThat(values).containsExactlyElementsOf(
                Range.until(monotonic.imageSize()));
    }
    
}
