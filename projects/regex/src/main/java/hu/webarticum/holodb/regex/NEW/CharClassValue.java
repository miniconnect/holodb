package hu.webarticum.holodb.regex.NEW;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class CharClassValue implements TrieValue {
	
	private final ImmutableList<Character> characters;

	public CharClassValue(ImmutableList<Character> characters) {
		this.characters = characters;
	}
	
	@Override
	public LargeInteger factor() {
		return LargeInteger.of(characters.size());
	}
	
	public ImmutableList<Character> characters() {
		return characters;
	}

}
