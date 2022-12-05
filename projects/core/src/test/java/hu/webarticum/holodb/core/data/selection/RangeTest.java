package hu.webarticum.holodb.core.data.selection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import hu.webarticum.miniconnect.lang.LargeInteger;

class RangeTest {

    @Test
    void testInvalidParameters() {
        assertThatThrownBy(() -> Range.fromUntil(large(10), large(2))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Range.fromUntil(large(-1), large(-7))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Range.fromUntil(large(10), large(-7))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testUntilToLength() {
        assertThat(Range.fromUntil(large(0), large(0)).size()).isEqualTo(large(0));
        assertThat(Range.fromUntil(large(0), large(75)).size()).isEqualTo(large(75));
        assertThat(Range.fromUntil(large(51), large(51)).size()).isEqualTo(large(0));
        assertThat(Range.fromUntil(large(63), large(121)).size()).isEqualTo(large(58));
        assertThat(Range.fromUntil(large("68934582407823419455"), large("73528383487528935867")).size())
                .isEqualTo(large("4593801079705516412"));
    }

    @Test
    void testLengthToUntil() {
        assertThat(Range.fromSize(large(0), large(0)).until()).isEqualTo(large(0));
        assertThat(Range.fromSize(large(0), large(42)).until()).isEqualTo(large(42));
        assertThat(Range.fromSize(large(79), large(0)).until()).isEqualTo(large(79));
        assertThat(Range.fromSize(large(83), large(115)).until()).isEqualTo(large(198));
        assertThat(Range.fromSize(large("92349872984752345923"), large("8784573894673012303")).until())
                .isEqualTo(large("101134446879425358226"));
    }

    @Test
    void testIterator() {
        assertThat(Range.fromUntil(large(0), large(0))).containsExactlyElementsOf(Collections.emptyList());
        assertThat(Range.fromUntil(large(0), large(11))).containsExactlyElementsOf(
                Arrays.asList(larges(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)));
        assertThat(Range.fromUntil(large(9), large(23))).containsExactlyElementsOf(
                Arrays.asList(larges(9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22)));
        assertThat(Range.fromUntil(
                large("27346871304718347645349017307248"),
                large("27346871304718347645349017307261"))).containsExactlyElementsOf(
                        Arrays.asList(larges(
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
        assertThat(Range.fromUntil(large(0), large(0)).reverseOrder()).containsExactlyElementsOf(Collections.emptyList());
        assertThat(Range.fromUntil(large(0), large(11)).reverseOrder()).containsExactlyElementsOf(
                Arrays.asList(larges(10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0)));
        assertThat(Range.fromUntil(large(9), large(23)).reverseOrder()).containsExactlyElementsOf(
                Arrays.asList(larges(22, 21, 20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9)));
        assertThat(Range
                .fromUntil(large("27346871304718347645349017307248"), large("27346871304718347645349017307261"))
                .reverseOrder()
                ).containsExactlyElementsOf(
                        Arrays.asList(larges(
                                "27346871304718347645349017307260", "27346871304718347645349017307259",
                                "27346871304718347645349017307258", "27346871304718347645349017307257",
                                "27346871304718347645349017307256", "27346871304718347645349017307255",
                                "27346871304718347645349017307254", "27346871304718347645349017307253",
                                "27346871304718347645349017307252", "27346871304718347645349017307251",
                                "27346871304718347645349017307250", "27346871304718347645349017307249",
                                "27346871304718347645349017307248")));
    }

    
    private static LargeInteger large(int value) {
        return LargeInteger.of(value);
    }

    private static LargeInteger large(String value) {
        return LargeInteger.of(value);
    }

    private static LargeInteger[] larges(int... values) {
        LargeInteger[] result = new LargeInteger[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = LargeInteger.of(values[i]);
        }
        return result;
    }

    private static LargeInteger[] larges(String... values) {
        LargeInteger[] result = new LargeInteger[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = LargeInteger.of(values[i]);
        }
        return result;
    }
    
}
