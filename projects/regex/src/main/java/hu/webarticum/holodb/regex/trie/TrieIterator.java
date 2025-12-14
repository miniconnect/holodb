package hu.webarticum.holodb.regex.trie;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import hu.webarticum.holodb.regex.charclass.CharClass;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class TrieIterator implements Iterator<String> {

    private final LinkedList<StackEntry> stack;


    private TrieIterator(LinkedList<StackEntry> stack) {
        this.stack = stack;
    }

    public static TrieIterator fromBeginning(TrieNode rootNode) {
        LinkedList<StackEntry> stack = new LinkedList<>();
        collectLeftTrace(rootNode, stack);
        return new TrieIterator(stack);
    }

    public static TrieIterator fromPosition(TrieNode rootNode, LargeInteger position) {
        LinkedList<StackEntry> stack = new LinkedList<>();
        if (position.isGreaterThanOrEqualTo(rootNode.size())) {
            return new TrieIterator(stack);
        }
        TrieNode subNode = rootNode;
        LargeInteger subPosition = position;
        while (!subNode.isLeaf()) {
            LargeInteger childrenFullSize = subNode.childrenFullSize();
            LargeInteger[] quotientAndRemainder = subPosition.divideAndRemainder(childrenFullSize);
            int charPosition = quotientAndRemainder[0].intValue();
            LargeInteger remainder = quotientAndRemainder[1];
            ImmutableList<LargeInteger> positions = subNode.positions();
            int foundIndex = positions.binarySearch(remainder.increment());
            if (foundIndex < 0) {
                foundIndex = -(foundIndex + 1);
            }
            LargeInteger childStartPosition = (foundIndex == 0) ? LargeInteger.ZERO : positions.get(foundIndex - 1);
            stack.add(new StackEntry(subNode, charPosition, foundIndex));
            subNode = subNode.children().get(foundIndex);
            subPosition = remainder.subtract(childStartPosition);
        }
        return new TrieIterator(stack);
    }

    private static void collectLeftTrace(TrieNode rootNode, LinkedList<StackEntry> stack) {
        TrieNode subNode = rootNode;
        while (!subNode.isLeaf()) {
            stack.add(new StackEntry(subNode, 0, 0));
            subNode = subNode.children().get(0);
        }
    }


    @Override
    public boolean hasNext() {
        return !stack.isEmpty();
    }

    @Override
    public String next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        String result = buildCurrentString();
        moveNext();
        return result;
    }

    private String buildCurrentString() {
        StringBuilder resultBuilder = new StringBuilder();
        Iterator<StackEntry> iterator = stack.iterator();
        iterator.next();
        while (iterator.hasNext()) {
            StackEntry stackEntry = iterator.next();
            if (stackEntry.node.isInner()) {
                CharClass charClass = stackEntry.node.charClass();
                char c;
                try {
                    c = charClass.chars().charAt(stackEntry.charPosition);
                } catch (Exception e) {
                    throw e;
                }
                resultBuilder.append(c);
            }
        }
        return resultBuilder.toString();
    }

    private void moveNext() {
        while (!stack.isEmpty()) {
            StackEntry stackEntry = stack.getLast();
            ImmutableList<TrieNode> children = stackEntry.node.children();
            int childCount = children.size();
            if (stackEntry.childPosition < childCount - 1) {
                stackEntry.childPosition++;
                break;
            }
            int charCount = stackEntry.node.charClass().chars().length();
            if (stackEntry.charPosition < charCount - 1) {
                stackEntry.charPosition++;
                stackEntry.childPosition = 0;
                break;
            }
            stack.removeLast();
        }
        if (!stack.isEmpty()) {
            StackEntry stackEntry = stack.getLast();
            TrieNode subNode = stackEntry.node.children().get(stackEntry.childPosition);
            collectLeftTrace(subNode, stack);
        }
    }


    private static class StackEntry {

        final TrieNode node;

        int charPosition;

        int childPosition;

        public StackEntry(TrieNode node, int charPosition, int childPosition) {
            this.node = node;
            this.charPosition = charPosition;
            this.childPosition = childPosition;
        }

    }

}
