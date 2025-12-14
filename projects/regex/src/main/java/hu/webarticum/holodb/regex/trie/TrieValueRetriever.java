package hu.webarticum.holodb.regex.trie;

import hu.webarticum.holodb.regex.charclass.CharClass;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class TrieValueRetriever {

    public String retrieve(TrieNode trieNode, LargeInteger position) {
        StringBuilder resultBuilder= new StringBuilder();
        retrieve(trieNode, position, resultBuilder);
        return resultBuilder.toString();
    }

    private void retrieve(TrieNode trieNode, LargeInteger position, StringBuilder stringBuilder) {
        if (trieNode.isLeaf()) {
            return;
        }

        LargeInteger subPosition = position;
        if (!trieNode.isRoot()) {
            CharClass charClass = trieNode.charClass();
            String chars = charClass.chars();
            int charCount = chars.length();

            if (charCount == 1) {
                stringBuilder.append(chars.charAt(0));
            } else {
                LargeInteger[] charIndexAndSubPosition = position.divideAndRemainder(trieNode.childrenFullSize());
                int charIndex = charIndexAndSubPosition[0].intValue();
                stringBuilder.append(chars.charAt(charIndex));
                subPosition = charIndexAndSubPosition[1];
            }
        }

        ImmutableList<LargeInteger> positions = trieNode.positions();
        int foundIndex = positions.binarySearch(subPosition.increment());
        if (foundIndex < 0) {
            foundIndex = -(foundIndex + 1);
        }
        LargeInteger childStartPosition = (foundIndex == 0) ? LargeInteger.ZERO : positions.get(foundIndex - 1);
        LargeInteger childSubPosition = subPosition.subtract(childStartPosition);
        TrieNode childTrieNode = trieNode.children().get(foundIndex);
        retrieve(childTrieNode, childSubPosition, stringBuilder);
    }

}
