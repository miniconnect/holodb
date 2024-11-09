package hu.webarticum.holodb.regex.NEW;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.util.Arrays;
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

    @Test
    void testSplit() {
    	
    	// TODO
    	
    }

    private CharClass charClassOf(String chars) {
    	return CharClass.of(ImmutableList.fromCharArray(chars), (c1, c2) -> c1.compareTo(c2));
    }
    
}
