package hu.webarticum.holodb.regex.NEW;

import hu.webarticum.miniconnect.lang.LargeInteger;

public class RootValueSet implements TrieValue {

	@Override
	public LargeInteger factor() {
		return LargeInteger.ONE;
	}

}
