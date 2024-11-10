package hu.webarticum.holodb.regex.NEW.charclass;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Comparator;

import org.junit.jupiter.api.Test;

import hu.webarticum.miniconnect.lang.ImmutableList;

class CharClassTest {

    @Test
    void testOfAndCharacters() {
    	ImmutableList<Character> characters = ImmutableList.fromCharArray(new char[] {'a', 'x'});
    	CharClass charClass = CharClass.of(characters, (c1, c2) -> c1.compareTo(c2));
    	assertThat(charClass.characters()).isEqualTo(characters);
    }

    @Test
    void testOfAndCharactersCustomComparator() {
    	ImmutableList<Character> characters = ImmutableList.fromCharArray(new char[] {'a', 'x'});
    	CharClass charClass = CharClass.of(characters, (c1, c2) -> 0);
    	assertThat(charClass.characters()).isEqualTo(ImmutableList.fromCharArray(new char[] {'a'}));
    }

    @Test
    void testSize() {
    	assertThat(charClassOf("axzx").size().intValueExact()).isEqualTo(3);
    }

    @Test
    void testCharacters() {
        assertThat(charClassOf("loremipsum").characters()).isEqualTo(ImmutableList.fromCharArray("eilmoprsu"));
    }

    @Test
    void testCompareEqual() {
        assertThat(charClassOf("abc")).isEqualByComparingTo(charClassOf("abc"));
    }

    @Test
    void testCompareLeftPrefix() {
        assertThat(charClassOf("abc")).isLessThan(charClassOf("abcdef"));
    }

    @Test
    void testCompareRightPrefix() {
        assertThat(charClassOf("abc")).isGreaterThan(charClassOf("ab"));
    }

    @Test
    void testCompareCommonPrefix() {
        assertThat(charClassOf("abcdef")).isLessThan(charClassOf("abcfgh"));
    }

    @Test
    void testCompareNoCommonPrefix() {
        assertThat(charClassOf("xz")).isGreaterThan(charClassOf("dg"));
    }

    @Test
    void testCompareJump() {
        assertThat(charClassOf("aklm")).isLessThan(charClassOf("cd"));
    }

    @Test
    void testHashCode() {
    	assertThat(charClassOf("loremipsum")).hasSameHashCodeAs(charClassOf("loremipsum"));
    }

    @Test
    void testEquals() {
    	assertThat(charClassOf("loremipsum")).isEqualTo(charClassOf("loremipsum"));
    }

    @Test
    void testNotEquals() {
    	assertThat(charClassOf("loremipsum")).isNotEqualTo(charClassOf("dolorsit"));
    }

    @Test
    void testUnion() {
    	assertThat(charClassOf("lorem").union(charClassOf("dolor"))).isEqualTo(charClassOf("delmor"));
    }

    @Test
    void testIntersection() {
    	assertThat(charClassOf("lorem").intersection(charClassOf("dolor"))).isEqualTo(charClassOf("lor"));
    }

    private CharClass charClassOf(String chars) {
    	return CharClass.of(ImmutableList.fromCharArray(chars), Comparator.naturalOrder());
    }
    
}
