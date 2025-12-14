package hu.webarticum.holodb.regex.charclass;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import hu.webarticum.holodb.regex.comparator.CharComparator;
import hu.webarticum.miniconnect.lang.ImmutableList;

class CharClassTest {

    private CharComparator comparator = Character::compare;

    @Test
    void testComparator() {
        ImmutableList<CharClass> inputs = ImmutableList.of(
                CharClass.of("", comparator),
                CharClass.of("lorem", comparator));
        assertThat(inputs).allMatch(cc -> cc.charComparator() == comparator);
    }

    @Test
    void testOfAndCharactersSorted() {
        ImmutableList<CharClass> inputs = ImmutableList.of(
                CharClass.of("", comparator),
                CharClass.of("abc", comparator),
                CharClass.of("123456789", comparator));
        assertThat(inputs.map(cc -> cc.chars())).containsExactly("", "abc", "123456789");
    }

    @Test
    void testOfAndCharactersUnsorted() {
        ImmutableList<CharClass> inputs = ImmutableList.of(
                CharClass.of("xatsu", comparator),
                CharClass.of("2563408917", comparator));
        assertThat(inputs.map(cc -> cc.chars())).containsExactly("astux", "0123456789");
    }

    @Test
    void testOfAndCharactersUnsortedNonUnique() {
        ImmutableList<CharClass> inputs = ImmutableList.of(
                CharClass.of("bcbxtsuuaa", comparator),
                CharClass.of("2734637374682833", comparator));
        assertThat(inputs.map(cc -> cc.chars())).containsExactly("abcstux", "234678");
    }

    @Test
    void testIndexOfEmpty() {
        CharClass charClass = CharClass.of("", comparator);
        assertThat(ImmutableList.of('a', 'b', 'c', 'd', 'e', 'f').map(charClass::indexOf)).containsExactly(
                -1, -1, -1, -1, -1, -1);
    }

    @Test
    void testIndexOfSingle() {
        CharClass charClass = CharClass.of("d", comparator);
        assertThat(ImmutableList.of('a', 'b', 'c', 'd', 'e', 'f').map(charClass::indexOf)).containsExactly(
                -1, -1, -1, 0, -2, -2);
    }

    @Test
    void testIndexOfTwoChars() {
        CharClass charClass = CharClass.of("ce", comparator);
        assertThat(ImmutableList.of('a', 'b', 'c', 'd', 'e', 'f').map(charClass::indexOf)).containsExactly(
                -1, -1, 0, -2, 1, -3);
    }

    @Test
    void testIndexOfManyChars() {
        CharClass charClass = CharClass.of("befghkmpstwx", comparator);
        assertThat(ImmutableList.of(
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
                'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
        ).map(charClass::indexOf)).containsExactly(
                -1, 0, -2, -2, 1, 2, 3, 4, -6, -6, 5, -7, 6, -8,
                -8, 7, -9, -9, 8, 9, -11, -11, 10, 11, -13, -13);
    }

    @Test
    void testUnion() {
        CharClass empty = CharClass.of("", comparator);
        CharClass early = CharClass.of("abdfh", comparator);
        CharClass late = CharClass.of("kmpxz", comparator);
        CharClass mixed1 = CharClass.of("aekpy", comparator);
        CharClass mixed2 = CharClass.of("befst", comparator);
        CharClass mixed3 = CharClass.of("bestu", comparator);
        ImmutableList<CharClass> inputs = ImmutableList.of(
                empty.union(empty),
                empty.union(mixed1),
                mixed2.union(empty),
                mixed1.union(mixed1),
                early.union(late),
                early.union(mixed1),
                mixed1.union(mixed2),
                mixed2.union(mixed3));
        assertThat(inputs.map(cc -> cc.chars())).containsExactly(
                "", "aekpy", "befst", "aekpy", "abdfhkmpxz", "abdefhkpy", "abefkpsty", "befstu");
    }

    @Test
    void testIntersecion() {
        CharClass empty = CharClass.of("", comparator);
        CharClass early = CharClass.of("abdfh", comparator);
        CharClass late = CharClass.of("kmpxz", comparator);
        CharClass mixed1 = CharClass.of("aekpy", comparator);
        CharClass mixed2 = CharClass.of("befst", comparator);
        CharClass mixed3 = CharClass.of("bestu", comparator);
        ImmutableList<CharClass> inputs = ImmutableList.of(
                empty.intersection(empty),
                empty.intersection(mixed1),
                mixed2.intersection(empty),
                mixed1.intersection(mixed1),
                early.intersection(late),
                early.intersection(mixed1),
                mixed1.intersection(mixed2),
                mixed2.intersection(mixed3));
        assertThat(inputs.map(cc -> cc.chars())).containsExactly("", "", "", "aekpy", "", "a", "e", "best");
    }

}
