package hu.webarticum.holodb.regex.NEW.comparator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Test;

class CharStringComparatorTest {

    @Test
    void testNaturalSimpleStrings() {
        CharStringComparator comparator = new CharStringComparator(Character::compare);
        List<String> strings = stringsOf("aa", "be", "bz", "", "acb", "", "be", "a", "ab");
        Collections.sort(strings, comparator);
        assertThat(strings).containsExactly("", "", "a", "aa", "ab", "acb", "be", "be", "bz");
    }

    @Test
    void testDefaultSimpleStrings() {
        CharStringComparator comparator = new CharStringComparator(new DefaultCharComparator());
        List<String> strings = stringsOf("aa", "be", "bz", "", "acb", "", "be", "a", "ab");
        Collections.sort(strings, comparator);
        assertThat(strings).containsExactly("", "", "a", "aa", "ab", "acb", "be", "be", "bz");
    }

    @Test
    void testDefaultComplexStrings() {
        CharStringComparator comparator = new CharStringComparator(new DefaultCharComparator());
        List<String> strings = stringsOf(
                "z9", "9a", "Ab", "a", "be", ",b", "áb", "", "3=", "á", "zu", "ab",
                "", "Ba", ",B", "a", "a!", "á", "Áb", "Z", ",b", ",,", ",7");
        Collections.sort(strings, comparator);
        assertThat(strings).containsExactly(
                "", "", ",,", ",7", ",B", ",b", ",b", "3=", "9a", "Ab", "a", "a",
                "a!", "ab","Áb", "á", "á", "áb", "Ba", "be", "Z", "z9", "zu");
    }
    
    @SuppressWarnings("unchecked")
    private List<String> stringsOf(String... strings) {
        return new ArrayList<>((List<String>) (List<?>) Arrays.asList(strings));
    }

}
