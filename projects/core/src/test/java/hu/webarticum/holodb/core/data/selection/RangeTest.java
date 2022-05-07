package hu.webarticum.holodb.core.data.selection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

class RangeTest {

    @Test
    void testInvalidParameters() {
        assertThatThrownBy(() -> Range.fromUntil(big(10), big(2))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Range.fromUntil(big(-1), big(-7))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Range.fromUntil(big(10), big(-7))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testUntilToLength() {
        assertThat(Range.fromUntil(big(0), big(0)).size()).isEqualTo(big(0));
        assertThat(Range.fromUntil(big(0), big(75)).size()).isEqualTo(big(75));
        assertThat(Range.fromUntil(big(51), big(51)).size()).isEqualTo(big(0));
        assertThat(Range.fromUntil(big(63), big(121)).size()).isEqualTo(big(58));
        assertThat(Range.fromUntil(big("68934582407823419455"), big("73528383487528935867")).size())
                .isEqualTo(big("4593801079705516412"));
    }

    @Test
    void testLengthToUntil() {
        assertThat(Range.fromSize(big(0), big(0)).until()).isEqualTo(big(0));
        assertThat(Range.fromSize(big(0), big(42)).until()).isEqualTo(big(42));
        assertThat(Range.fromSize(big(79), big(0)).until()).isEqualTo(big(79));
        assertThat(Range.fromSize(big(83), big(115)).until()).isEqualTo(big(198));
        assertThat(Range.fromSize(big("92349872984752345923"), big("8784573894673012303")).until())
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

    @Test
    void testReverseOrder() {
        assertThat(Range.fromUntil(big(0), big(0)).reverseOrder()).containsExactlyElementsOf(Collections.emptyList());
        assertThat(Range.fromUntil(big(0), big(11)).reverseOrder()).containsExactlyElementsOf(
                Arrays.asList(bigs(10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0)));
        assertThat(Range.fromUntil(big(9), big(23)).reverseOrder()).containsExactlyElementsOf(
                Arrays.asList(bigs(22, 21, 20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9)));
        assertThat(Range
                .fromUntil(big("27346871304718347645349017307248"), big("27346871304718347645349017307261"))
                .reverseOrder()
                ).containsExactlyElementsOf(
                        Arrays.asList(bigs(
                                "27346871304718347645349017307260", "27346871304718347645349017307259",
                                "27346871304718347645349017307258", "27346871304718347645349017307257",
                                "27346871304718347645349017307256", "27346871304718347645349017307255",
                                "27346871304718347645349017307254", "27346871304718347645349017307253",
                                "27346871304718347645349017307252", "27346871304718347645349017307251",
                                "27346871304718347645349017307250", "27346871304718347645349017307249",
                                "27346871304718347645349017307248")));
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
