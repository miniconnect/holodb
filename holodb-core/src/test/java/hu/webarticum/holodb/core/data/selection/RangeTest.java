package hu.webarticum.holodb.core.data.selection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

class RangeTest {

    @Test
    void testInvalidParameters() {
        assertThatThrownBy(() -> Range.fromUntil(big(10), big(2))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Range.fromUntil(big(-1), big(-7))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Range.fromUntil(big(10), big(-7))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testUntilToSize() {
        assertThat(Range.fromUntil(big(0), big(0)).size()).isEqualTo(big(0));
        assertThat(Range.fromUntil(big(0), big(75)).size()).isEqualTo(big(75));
        assertThat(Range.fromUntil(big(51), big(51)).size()).isEqualTo(big(0));
        assertThat(Range.fromUntil(big(63), big(121)).size()).isEqualTo(big(58));
        assertThat(Range.fromUntil(big("68934582407823419455"), big("73528383487528935867")).size())
                .isEqualTo(big("4593801079705516412"));
    }

    @Test
    void testSizeToUntil() {
        assertThat(Range.fromSize(big(0), big(0)).until()).isEqualTo(big(0));
        assertThat(Range.fromSize(big(0), big(42)).until()).isEqualTo(big(42));
        assertThat(Range.fromSize(big(79), big(0)).until()).isEqualTo(big(79));
        assertThat(Range.fromSize(big(83), big(115)).until()).isEqualTo(big(198));
        assertThat(Range.fromSize(big("92349872984752345923"), big("8784573894673012303")).until())
                .isEqualTo(big("101134446879425358226"));
    }

    
    private static BigInteger big(int value) {
        return BigInteger.valueOf(value);
    }

    private static BigInteger big(String value) {
        return new BigInteger(value);
    }

}
