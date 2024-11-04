package hu.webarticum.holodb.regex.NEW;

import hu.webarticum.miniconnect.lang.LargeInteger;

public class MultiplierValue implements TrieValue {

	private final LargeInteger factor;

	private final CharClassValue lookAheadCharClass;
	
	public MultiplierValue(LargeInteger factor, CharClassValue lookAheadCharClass) {
		this.factor = factor;
		this.lookAheadCharClass = lookAheadCharClass;
	}

	public LargeInteger factor() {
		return factor;
	}
	
	public CharClassValue lookAheadCharClass() {
		return lookAheadCharClass;
	}

}
