package hu.webarticum.holodb.regex.trie;

import hu.webarticum.holodb.regex.charclass.CharClass;
import hu.webarticum.miniconnect.lang.FindPositionResult;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class TrieValueLocator {

    public FindPositionResult locate(TrieNode trieNode, String value) {
        String normalizedValue = TrieNode.ROOT_CHAR + value + TrieNode.LEAF_CHAR;
        return locate(trieNode, LargeInteger.ZERO, normalizedValue, 0);
    }

    private FindPositionResult locate(TrieNode trieNode, LargeInteger offset, String value, int level) {
        LargeInteger childrenFullSize = trieNode.childrenFullSize();
        char currentChar = value.charAt(level);
        int foundIndex = trieNode.charClass().indexOf(currentChar);
        if (foundIndex < 0) {
            int notfoundIndex = -(foundIndex + 1);
            LargeInteger insertPosition = childrenFullSize.multiply(LargeInteger.of(notfoundIndex)).add(offset);
            return FindPositionResult.notFound(insertPosition);
        }
        LargeInteger subOffset = childrenFullSize.multiply(LargeInteger.of(foundIndex)).add(offset);
        if (trieNode.isLeaf()) {
            if (level < value.length() - 1) {
                return FindPositionResult.notFound(subOffset.increment());
            } else {
                return FindPositionResult.found(subOffset);
            }
        }
        ImmutableList<TrieNode> children = trieNode.children();
        int nextLevel = level + 1;
        char nextChar = value.charAt(nextLevel);
        int childIndex = findAcceptingChild(children, nextChar);
        LargeInteger childRelativeOffset = childIndex == 0 ?
                LargeInteger.ZERO :
                trieNode.positions().get(childIndex - 1);
        LargeInteger childOffset = subOffset.add(childRelativeOffset);
        TrieNode childNode = children.get(childIndex);
        return locate(childNode, childOffset, value, nextLevel);
    }
    
    private int findAcceptingChild(ImmutableList<TrieNode> children, char c) {
        int length = children.size();
        if (length < 2) {
            return 0;
        }
        TrieNode firstChild = children.get(0);
        int firstCmp = compareFirst(c, firstChild);
        if (firstCmp <= 0) {
            return 0;
        }
        int lastPos = length - 1;
        TrieNode lastChild = children.get(lastPos);
        int lastCmp = compareFirst(c, lastChild);
        if (lastCmp >= 0) {
            return lastPos;
        }
        int low = 0;
        int high = lastPos;
        while (true) {
            int middle = (low + high) >>> 1;
            if (middle == low) {
                return low;
            }
            TrieNode middleChild = children.get(middle);
            int middleCmp = compareFirst(c, middleChild);
            if (middleCmp == 0) {
                return middle;
            } else if (middleCmp < 0) {
                high = middle;
            } else {
                low = middle;
            }
        }
    }
    
    private int compareFirst(char c, TrieNode childNode) {
        CharClass charClass = childNode.charClass();
        char firstChar = charClass.chars().charAt(0);
        return charClass.charComparator().compare(c, firstChar);
    }
    
}
