package hu.webarticum.holodb.regex.trie;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import hu.webarticum.holodb.regex.charclass.CharClass;
import hu.webarticum.holodb.regex.comparator.CharComparator;
import hu.webarticum.miniconnect.lang.ImmutableList;

public final class TestUtil {

    private TestUtil() {
        // utility class
    }

    public static <T> List<T> fetchN(Iterator<T> iterator, int n) {
        List<T> result = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            result.add(iterator.next());
        }
        return result;
    }

    public static TrieNode generateDigitsTrie(
            int numberOfDigits, ImmutableList<TrieNode> tailNodes, CharComparator charComparator) {
        CharClass digits1 = CharClass.of("012", charComparator);
        CharClass digits2 = CharClass.of("34567", charComparator);
        CharClass digits3 = CharClass.of("89", charComparator);
        ImmutableList<TrieNode> nodes = tailNodes;
        for (int i = 0; i < numberOfDigits; i++) {
            nodes = ImmutableList.of(
                    TrieNode.of(digits1, nodes),
                    TrieNode.of(digits2, nodes),
                    TrieNode.of(digits3, nodes));
        }
        return TrieNode.rootOf(charComparator, nodes);
    }

}
