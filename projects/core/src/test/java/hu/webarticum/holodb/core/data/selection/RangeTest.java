package hu.webarticum.holodb.core.data.selection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import hu.webarticum.miniconnect.lang.LargeInteger;

class RangeTest {

    @Test
    void testInvalidParameters() {
        assertThatThrownBy(() -> Range.fromUntil(LargeInteger.of(10), LargeInteger.of(2)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Range.fromUntil(LargeInteger.of(-1), LargeInteger.of(-7)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Range.fromUntil(LargeInteger.of(10), LargeInteger.of(-7)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testUntilToLength() {
        assertThat(Range.fromUntil(LargeInteger.ZERO, LargeInteger.ZERO).size()).isEqualTo(LargeInteger.ZERO);
        assertThat(Range.fromUntil(LargeInteger.ZERO, LargeInteger.of(75)).size()).isEqualTo(LargeInteger.of(75));
        assertThat(Range.fromUntil(LargeInteger.of(51), LargeInteger.of(51)).size()).isEqualTo(LargeInteger.ZERO);
        assertThat(Range.fromUntil(LargeInteger.of(63), LargeInteger.of(121)).size()).isEqualTo(LargeInteger.of(58));
        assertThat(Range.fromUntil(
                        LargeInteger.of("68934582407823419455"),
                        LargeInteger.of("73528383487528935867")
                ).size())
                .isEqualTo(LargeInteger.of("4593801079705516412"));
    }

    @Test
    void testLengthToUntil() {
        assertThat(Range.fromSize(LargeInteger.ZERO, LargeInteger.ZERO).until()).isEqualTo(LargeInteger.ZERO);
        assertThat(Range.fromSize(LargeInteger.ZERO, LargeInteger.of(42)).until()).isEqualTo(LargeInteger.of(42));
        assertThat(Range.fromSize(LargeInteger.of(79), LargeInteger.ZERO).until()).isEqualTo(LargeInteger.of(79));
        assertThat(Range.fromSize(LargeInteger.of(83), LargeInteger.of(115)).until()).isEqualTo(LargeInteger.of(198));
        assertThat(Range.fromSize(LargeInteger.of("92349872984752345923"), LargeInteger.of("8784573894673012303")).until())
                .isEqualTo(LargeInteger.of("101134446879425358226"));
    }

    @Test
    void testIterator() {
        assertThat(Range.fromUntil(LargeInteger.ZERO, LargeInteger.ZERO)).isEmpty();
        assertThat(Range.fromUntil(LargeInteger.ZERO, LargeInteger.of(11))).containsExactly(
                LargeInteger.arrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        assertThat(Range.fromUntil(LargeInteger.of(9), LargeInteger.of(23))).containsExactly(
                LargeInteger.arrayOf(9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22));
        assertThat(Range.fromUntil(
                LargeInteger.of("27346871304718347645349017307248"),
                LargeInteger.of("27346871304718347645349017307261"))).containsExactlyElementsOf(
                        Arrays.asList(LargeInteger.arrayOf(
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
        assertThat(Range.fromUntil(LargeInteger.ZERO, LargeInteger.ZERO).reverseOrder()).isEmpty();
        assertThat(Range.fromUntil(LargeInteger.ZERO, LargeInteger.of(11)).reverseOrder()).containsExactly(
                LargeInteger.arrayOf(10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0));
        assertThat(Range.fromUntil(LargeInteger.of(9), LargeInteger.of(23)).reverseOrder()).containsExactly(
                LargeInteger.arrayOf(22, 21, 20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9));
        assertThat(Range
                .fromUntil(
                        LargeInteger.of("27346871304718347645349017307248"),
                        LargeInteger.of("27346871304718347645349017307261"))
                .reverseOrder()
                ).containsExactlyElementsOf(
                        Arrays.asList(LargeInteger.arrayOf(
                                "27346871304718347645349017307260", "27346871304718347645349017307259",
                                "27346871304718347645349017307258", "27346871304718347645349017307257",
                                "27346871304718347645349017307256", "27346871304718347645349017307255",
                                "27346871304718347645349017307254", "27346871304718347645349017307253",
                                "27346871304718347645349017307252", "27346871304718347645349017307251",
                                "27346871304718347645349017307250", "27346871304718347645349017307249",
                                "27346871304718347645349017307248")));
    }
    
}
