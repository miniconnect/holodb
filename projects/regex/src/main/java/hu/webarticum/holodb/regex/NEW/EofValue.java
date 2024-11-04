package hu.webarticum.holodb.regex.NEW;

import hu.webarticum.miniconnect.lang.LargeInteger;

public class EofValue implements TrieValue {

	@Override
	public LargeInteger factor() {
		return LargeInteger.ONE;
	}

}
