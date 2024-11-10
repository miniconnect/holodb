package hu.webarticum.holodb.regex.NEW.charclass;

import java.util.Comparator;
import java.util.Objects;
import java.util.TreeSet;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;
import hu.webarticum.miniconnect.lang.ToStringBuilder;

public class CharClass implements Comparable<CharClass> {
	
	private final ImmutableList<Character> characters;
	
	private final Comparator<Character> characterComparator;

	private CharClass(ImmutableList<Character> characters, Comparator<Character> characterComparator) {
		this.characters = characters;
		this.characterComparator = characterComparator;
	}

	public static CharClass of(Iterable<Character> charIterable, Comparator<Character> characterComparator) {
		return new CharClass(ImmutableList.fromCollection(toTreeSet(charIterable, characterComparator)), characterComparator);
	}
	
	private static TreeSet<Character> toTreeSet(Iterable<Character> charIterable, Comparator<Character> characterComparator) {
		if (charIterable instanceof TreeSet && Objects.equals(((TreeSet<Character>) charIterable).comparator(), characterComparator)) {
			return (TreeSet<Character>) charIterable;
		}

		TreeSet<Character> characters = new TreeSet<>(characterComparator);
		charIterable.forEach(c -> characters.add(c));
		return characters;
	}

	public LargeInteger size() {
		return LargeInteger.of(characters.size());
	}

	public ImmutableList<Character> characters() {
		return characters;
	}

    public Comparator<Character> characterComparator() {
        return characterComparator;
    }
    
    @Override
    public int compareTo(CharClass other) {
        if (this == other) {
            return 0;
        }
        int size = characters.size();
        int otherSize = other.characters.size();
        int commonSize = Math.min(size, otherSize);
        for (int i = 0; i < commonSize; i++) {
            Character character = characters.get(i);
            Character otherCharacter = other.characters.get(i);
            int cmp = characterComparator.compare(character, otherCharacter);
            if (cmp != 0) {
                return cmp;
            }
        }
        if (size < otherSize) {
            return -1;
        } else if (size > otherSize) {
            return 1;
        } else {
            return 0;
        }
    }
    
	@Override
	public int hashCode() {
		return Objects.hash(characters, characterComparator);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (!(obj instanceof CharClass)) {
			return false;
		}
		CharClass otherCharClass = (CharClass) obj;
		return characters.equals(otherCharClass.characters) && Objects.equals(characterComparator, otherCharClass.characterComparator);
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.add("characters", characters)
				.add("characterComparator", characterComparator)
				.build();
	}
	
	public CharClass union(CharClass other) {
		TreeSet<Character> unionSet = new TreeSet<>(characters.asList());
		unionSet.addAll(other.characters.asList());
		return of(unionSet, characterComparator);
	}

	public CharClass intersection(CharClass other) {
		if (
				characters.isEmpty() ||
				other.characters.isEmpty() ||
				characters.first() > other.characters.last() ||
				characters.last() < other.characters.first()) {
			return new CharClass(ImmutableList.empty(), characterComparator);
		}
		TreeSet<Character> intersectionSet = new TreeSet<>(characters.asList());
		intersectionSet.retainAll(other.characters.asList());
		return of(intersectionSet, characterComparator);
	}

}
