package hu.webarticum.holodb.regex.NEW;

import java.util.ArrayList;
import java.util.List;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class TrieNode {
	
	private final TrieValue value;
	
	private final ImmutableList<TrieNode> children;
	
	private LargeInteger size = null;
	
	private ImmutableList<LargeInteger> childMap = null;
	
	public TrieNode(TrieValue value, ImmutableList<TrieNode> children) {
		this.value = value;
		this.children = children;
	}
	
	public TrieValue value() {
		return value;
	}

	public ImmutableList<TrieNode> children() {
		return children;
	}

	public LargeInteger size() {
		if (size == null) {
			calculateSize();
		}
		return size;
	}
	
	public ImmutableList<LargeInteger> childMap() {
		if (childMap == null) {
			calculateSize();
		}
		return childMap;
	}

	private synchronized void calculateSize() {
		if (size != null) {
			return;
		}
		int childCount = children.size();
		List<LargeInteger> childMapBuilder = new ArrayList<>(childCount);
		LargeInteger subSize = LargeInteger.ZERO;
		for (int i = 0; i < childCount; i++) {
			subSize = subSize.add(children.get(i).size());
			childMapBuilder.add(subSize);
		}
		size = subSize.max(LargeInteger.ONE).multiply(value.factor());
		childMap = ImmutableList.fromCollection(childMapBuilder);
	}

}
