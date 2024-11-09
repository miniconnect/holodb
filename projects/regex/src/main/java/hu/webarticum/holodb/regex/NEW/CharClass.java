package hu.webarticum.holodb.regex.NEW;

import java.util.Comparator;
import java.util.Objects;
import java.util.TreeSet;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;
import hu.webarticum.miniconnect.lang.ToStringBuilder;

public class CharClass {
	
	private final ImmutableList<Character> characters;
	
	private final Comparator<Character> comparator;

	private CharClass(ImmutableList<Character> characters, Comparator<Character> comparator) {
		this.characters = characters;
		this.comparator = comparator;
	}

	public static CharClass of(Iterable<Character> charIterable, Comparator<Character> comparator) {
		return new CharClass(ImmutableList.fromCollection(toTreeSet(charIterable, comparator)), comparator);
	}
	
	private static TreeSet<Character> toTreeSet(Iterable<Character> charIterable, Comparator<Character> comparator) {
		if (charIterable instanceof TreeSet && Objects.equals(((TreeSet<Character>) charIterable).comparator(), comparator)) {
			return (TreeSet<Character>) charIterable;
		}

		TreeSet<Character> characters = new TreeSet<>(comparator);
		charIterable.forEach(c -> characters.add(c));
		return characters;
	}

	public LargeInteger size() {
		return LargeInteger.of(characters.size());
	}

	public ImmutableList<Character> characters() {
		return characters;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(characters, comparator);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (!(obj instanceof CharClass)) {
			return false;
		}
		CharClass otherCharClass = (CharClass) obj;
		return characters.equals(otherCharClass.characters) && Objects.equals(comparator, otherCharClass.comparator);
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.add("characters", characters)
				.add("comparator", comparator)
				.build();
	}
	
	public CharClass union(CharClass other) {
		TreeSet<Character> unionSet = new TreeSet<>(characters.asList());
		unionSet.addAll(other.characters.asList());
		return of(unionSet, comparator);
	}

	public CharClass intersection(CharClass other) {
		if (
				characters.isEmpty() ||
				other.characters.isEmpty() ||
				characters.first() > other.characters.last() ||
				characters.last() < other.characters.first()) {
			return new CharClass(ImmutableList.empty(), comparator);
		}
		TreeSet<Character> intersectionSet = new TreeSet<>(characters.asList());
		intersectionSet.retainAll(other.characters.asList());
		return of(intersectionSet, comparator);
	}

	public ImmutableList<CharClass> split(ImmutableList<CharClass> others) {
		
		// TODO
		return null;
		
	}
	
}
