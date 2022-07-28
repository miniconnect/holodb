package hu.webarticum.holodb.core.data.source;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

import hu.webarticum.miniconnect.lang.ImmutableList;

class TransformingSourceTest {

    @Test
    void testBasics() {
        TransformingSource<Integer, String> source = createTransformingSource();
        assertThat(source.type()).isEqualTo(String.class);
        assertThat(source.size()).isEqualTo(big(8));
    }

    @Test
    void testGet() {
        TransformingSource<Integer, String> source = createTransformingSource();
        assertThat(source.get(big(3))).isEqualTo("24");
    }

    @Test
    void testPossibleValues() {
        TransformingSource<Integer, String> source = createTransformingSource();
        assertThat(ImmutableList.fill(source.size().intValue(), i -> source.get(big(i))))
                .containsExactly("4", "6", "3", "24", "35", "24", "12", "63");
    }

    
    private static TransformingSource<Integer, String> createTransformingSource() {
        Source<Integer> baseSource = new FixedSource<>(4, 6, 3, 24, 35, 24, 12, 63);
        return new TransformingSource<>(baseSource, String.class, i -> i.toString());
    }
    
    private static BigInteger big(int value) {
        return BigInteger.valueOf(value);
    }

}
