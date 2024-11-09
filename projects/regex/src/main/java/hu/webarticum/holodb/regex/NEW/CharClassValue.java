package hu.webarticum.holodb.regex.NEW;

import hu.webarticum.miniconnect.lang.LargeInteger;

public class CharClassValue implements TrieValue {
	
	private final CharClass charClass;

	public CharClassValue(CharClass charClass) {
		this.charClass = charClass;
	}
	
	@Override
	public LargeInteger factor() {
		return charClass.size();
	}
	
	public CharClass charClass() {
		return charClass;
	}

}
