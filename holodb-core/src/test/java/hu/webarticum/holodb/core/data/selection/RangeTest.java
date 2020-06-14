package hu.webarticum.holodb.core.data.selection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import hu.webarticum.holodb.core.data.selection.Range;

class RangeTest {

    @Test
    void testInvalidParameters() {
        assertThatThrownBy(() -> Range.fromUntil(big(10), big(2))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Range.fromUntil(big(-1), big(-7))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Range.fromUntil(big(10), big(-7))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testUntilToLength() {
        assertThat(Range.fromUntil(big(0), big(0)).getLength()).isEqualTo(big(0));
        assertThat(Range.fromUntil(big(0), big(75)).getLength()).isEqualTo(big(75));
        assertThat(Range.fromUntil(big(51), big(51)).getLength()).isEqualTo(big(0));
        assertThat(Range.fromUntil(big(63), big(121)).getLength()).isEqualTo(big(58));
        assertThat(Range.fromUntil(big("68934582407823419455"), big("73528383487528935867")).getLength())
                .isEqualTo(big("4593801079705516412"));
    }

    @Test
    void testLengthToUntil() {
        assertThat(Range.fromLength(big(0), big(0)).getUntil()).isEqualTo(big(0));
        assertThat(Range.fromLength(big(0), big(42)).getUntil()).isEqualTo(big(42));
        assertThat(Range.fromLength(big(79), big(0)).getUntil()).isEqualTo(big(79));
        assertThat(Range.fromLength(big(83), big(115)).getUntil()).isEqualTo(big(198));
        assertThat(Range.fromLength(big("92349872984752345923"), big("8784573894673012303")).getUntil())
                .isEqualTo(big("101134446879425358226"));
    }

    @Test
    void testIterator() {
        assertThat(Range.fromUntil(big(0), big(0))).containsExactlyElementsOf(Collections.emptyList());
        assertThat(Range.fromUntil(big(0), big(11))).containsExactlyElementsOf(
                Arrays.asList(bigs(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)));
        assertThat(Range.fromUntil(big(9), big(23))).containsExactlyElementsOf(
                Arrays.asList(bigs(9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22)));
        assertThat(Range.fromUntil(
                big("27346871304718347645349017307248"),
                big("27346871304718347645349017307261"))).containsExactlyElementsOf(
                        Arrays.asList(bigs(
                                "27346871304718347645349017307248", "27346871304718347645349017307249",
                                "27346871304718347645349017307250", "27346871304718347645349017307251",
                                "27346871304718347645349017307252", "27346871304718347645349017307253",
                                "27346871304718347645349017307254", "27346871304718347645349017307255",
                                "27346871304718347645349017307256", "27346871304718347645349017307257",
                                "27346871304718347645349017307258", "27346871304718347645349017307259",
                                "27346871304718347645349017307260")));
    }

    
    private static BigInteger big(int value) {
        return BigInteger.valueOf(value);
    }

    private static BigInteger big(String value) {
        return new BigInteger(value);
    }

    private static BigInteger[] bigs(int... values) {
        BigInteger[] result = new BigInteger[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = BigInteger.valueOf(values[i]);
        }
        return result;
    }

    private static BigInteger[] bigs(String... values) {
        BigInteger[] result = new BigInteger[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = new BigInteger(values[i]);
        }
        return result;
    }
    
}
