package hu.webarticum.holodb.regex.NEW.comparator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import hu.webarticum.miniconnect.lang.ImmutableList;

class DefaultCharComparatorTest {

    private final DefaultCharComparator comparator = new DefaultCharComparator();

    @Test
    void testMixedAscending() {
        checkAllAscending(ImmutableList.of("?9", "/0", "=a", ",Á", "7c", "3Ű"));
    }

    @Test
    void testOthersEqual() {
        checkAllEqual(ImmutableList.of("  ", "??", ",,", "==", "~~"));
    }

    @Test
    void testOthersAscending() {
        checkAllAscending(ImmutableList.of(" =", " ?", "=?", "=~", "}~"));
    }

    @Test
    void testDigitsEqual() {
        checkAllEqual(ImmutableList.of("00", "11", "22", "33", "44", "55", "66", "77", "88", "99"));
    }

    @Test
    void testDigitsAscending() {
        checkAllAscending(ImmutableList.of("01", "09", "23", "34", "57", "68", "69", "78", "89"));
    }

    @Test
    void testAlphabetictsEqual() {
        checkAllEqual(ImmutableList.of("aa", "cc", "μμ", "AA", "BB", "ΘΘ", "áá", "űű", "ᾠᾠ", "ÁÁ", "ŐŐ", "ÑÑ"));
    }

    @Test
    void testAlphabetictsSameBaseAndDiacriticsAscending() {
        checkAllAscending(ImmutableList.of("Aa", "Cc", "Áá", "Űű", "Δδ", "Ññ"));
    }

    @Test
    void testAlphabetictsSameBaseAndCaseAscending() {
        checkAllAscending(ImmutableList.of("aá", "AÁ", "oó", "oő", "ÓŐ", "óő", "nñ", "NÑ"));
    }

    @Test
    void testAlphabetictsSameBaseAscending() {
        checkAllAscending(ImmutableList.of("Aá", "aÁ", "Óö", "óÖ", "Uű", "uŰ", "Nñ", "nÑ"));
    }

    @Test
    void testAlphabetictsSameDiacriticsAndCaseAscending() {
        checkAllAscending(ImmutableList.of("ab", "AB", "áé", "ÁÉ", "no", "NO", "ñõ", "ÑÕ"));
    }

    @Test
    void testAlphabetictsSameDiacriticsAscending() {
        checkAllAscending(ImmutableList.of("aB", "áÉ", "öÜ", "őŰ", "nO", "ñÕ"));
    }

    @Test
    void testAlphabetictsSameCaseAscending() {
        checkAllAscending(ImmutableList.of("áb", "ÁB", "éö", "gű", "ÍŐ", "ñs", "ñᾠ"));
    }

    @Test
    void testAlphabetictsCompletelyUnrelatedAscending() {
        checkAllAscending(ImmutableList.of("áB", "Áb", "éŰ", "Sᾠ", "ñS", "Ñᾠ"));
    }

    private void checkAllEqual(ImmutableList<String> twoCharsList) {
        for (String twoChars : twoCharsList) {
            checkEqual(twoChars);
        }
    }
    
    private void checkEqual(String twoChars) {
        char a = twoChars.charAt(0);
        char b = twoChars.charAt(1);
        assertThat(comparator.compare(a, b)).as("'" + a + "' should be equal to '" + b + "'").isZero();
        assertThat(comparator.compare(b, a)).as("'" + b + "' should be equal to '" + a + "'").isZero();
    }

    private void checkAllAscending(ImmutableList<String> twoCharsList) {
        for (String twoChars : twoCharsList) {
            checkAscending(twoChars);
        }
    }
    
    private void checkAscending(String twoChars) {
        char a = twoChars.charAt(0);
        char b = twoChars.charAt(1);
        assertThat(comparator.compare(a, b)).as("'" + a + "' should be less than '" + b + "'").isNegative();
        assertThat(comparator.compare(b, a)).as("'" + b + "' should be greater than '" + a + "'").isPositive();
    }

}
