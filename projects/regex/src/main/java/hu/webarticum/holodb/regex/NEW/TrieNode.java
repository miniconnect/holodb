package hu.webarticum.holodb.regex.NEW;

import hu.webarticum.miniconnect.lang.FindPositionResult;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

public interface TrieNode {
	
	public LargeInteger size();

	public Object value();
	
	public ImmutableList<TrieNode> children();
	
	public ImmutableList<LargeInteger> childMap();
	
	public ImmutableList<Object> traceAt(LargeInteger index);
	
	public Iterable<ImmutableList<Object>> traces();
	
	public Iterable<ImmutableList<Object>> traces(LargeInteger from);
	
	public Iterable<ImmutableList<Object>> traces(LargeInteger from, LargeInteger until);
	
	public FindPositionResult find(ImmutableList<Object> trace);
	
}
