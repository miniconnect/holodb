package hu.webarticum.holodb.regex.NEW.trie;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import hu.webarticum.holodb.regex.NEW.charclass.CharClass;
import hu.webarticum.holodb.regex.NEW.comparator.CharComparator;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class TrieNode {
    
    public static final char ROOT_CHAR = '\u0002';
    
    public static final char LEAF_CHAR = '\u0004';
    

    private final CharClass charClass;
    
    private final ImmutableList<TrieNode> children;

    private final ImmutableList<LargeInteger> positions;

    private final LargeInteger size;
    
    private final int hashCode;
    

    private TrieNode(CharClass charClass, ImmutableList<TrieNode> children, ImmutableList<LargeInteger> positions) {
        this.charClass = charClass;
        this.children = children;
        this.positions = positions;
        this.size = this.childrenFullSize().multiply(this.charClassSize());
        this.hashCode = (charClass.hashCode() * 31) + children.hashCode();
    }

    public static TrieNode of(CharClass charClass, ImmutableList<TrieNode> children) {
        ImmutableList<LargeInteger> positions = extractPositions(children);
        return new TrieNode(charClass, children, positions);
    }

    public static TrieNode rootOf(CharComparator charComparator, ImmutableList<TrieNode> children) {
        CharClass charClass = CharClass.of(String.valueOf(ROOT_CHAR), charComparator);
        ImmutableList<LargeInteger> positions = extractPositions(children);
        return new TrieNode(charClass, children, positions);
    }
    
    public static TrieNode leafOf(CharComparator charComparator) {
        CharClass charClass = CharClass.of(String.valueOf(LEAF_CHAR), charComparator);
        ImmutableList<TrieNode> children = ImmutableList.of((TrieNode) null);
        ImmutableList<LargeInteger> positions = ImmutableList.of(LargeInteger.ONE);
        return new TrieNode(charClass, children, positions);
    }

    private static ImmutableList<LargeInteger> extractPositions(ImmutableList<TrieNode> children) {
        List<LargeInteger> resultBuilder= new ArrayList<>(children.size());
        LargeInteger size = LargeInteger.ZERO;
        for (TrieNode childNode : children) {
            size = size.add(childNode.size());
            resultBuilder.add(size);
        }
        return ImmutableList.fromCollection(resultBuilder);
    }

    
    public CharClass charClass() {
        return charClass;
    }

    public ImmutableList<TrieNode> children() {
        return children;
    }

    public ImmutableList<LargeInteger> positions() {
        return positions;
    }

    public LargeInteger childrenFullSize() {
        return positions.last();
    }

    public LargeInteger charClassSize() {
        return LargeInteger.of(charClass.size());
    }

    public LargeInteger size() {
        return size;
    }

    public boolean isRoot() {
        String chars = charClass.chars();
        return (chars.length() == 1) && (chars.charAt(0) == ROOT_CHAR);
    }
    
    public boolean isLeaf() {
        String chars = charClass.chars();
        return (chars.length() == 1) && (chars.charAt(0) == LEAF_CHAR) && (children.get(0) == null);
    }
    
    @Override
    public int hashCode() {
        return hashCode;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof TrieNode)) {
            return false;
        }
        TrieNode other = (TrieNode) obj;
        return Objects.equals(charClass, other.charClass) && Objects.equals(children, other.children);
    }
    
}
