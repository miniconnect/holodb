package hu.webarticum.holodb.regex.facade;

import java.util.Iterator;

import hu.webarticum.holodb.regex.algorithm.AstToTreeConverter;
import hu.webarticum.holodb.regex.algorithm.TreeSortingTransformer;
import hu.webarticum.holodb.regex.algorithm.TreeToTrieConverter;
import hu.webarticum.holodb.regex.algorithm.TreeWeedingTransformer;
import hu.webarticum.holodb.regex.ast.AlternationAstNode;
import hu.webarticum.holodb.regex.comparator.CharComparator;
import hu.webarticum.holodb.regex.comparator.DefaultCharComparator;
import hu.webarticum.holodb.regex.trie.TrieIterator;
import hu.webarticum.holodb.regex.parser.RegexParser;
import hu.webarticum.holodb.regex.tree.TreeNode;
import hu.webarticum.holodb.regex.trie.TrieNode;
import hu.webarticum.holodb.regex.trie.TrieValueLocator;
import hu.webarticum.holodb.regex.trie.TrieValueRetriever;
import hu.webarticum.miniconnect.lang.FindPositionResult;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class MatchList implements Iterable<String> {
    
    private final TrieNode trie;

    private MatchList(Builder builder, String pattern) {
        CharComparator charComparator = builder.supplyCharComparator();
        AlternationAstNode ast = (AlternationAstNode) new RegexParser().parse(pattern);
        int repeatLimit = builder.repeatLimit;
        int groupRepeatLimit = builder.groupRepeatLimit;
        TreeNode rawTree = new AstToTreeConverter(charComparator, repeatLimit, groupRepeatLimit).convert(ast);
        TreeNode compactTree = new TreeWeedingTransformer().weed(rawTree);
        TreeNode sortedTree = new TreeSortingTransformer().sort(compactTree);
        this.trie = new TreeToTrieConverter(charComparator).convert(sortedTree);
    }
    
    public static MatchList of(String pattern) {
        return builder().build(pattern);
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    
    public LargeInteger size() {
        return trie.size();
    }

    public String get(long i) {
        return get(LargeInteger.of(i));
    }

    public String get(LargeInteger i) {
        return new TrieValueRetriever().retrieve(trie, i);
    }
    
    public FindPositionResult find(String value) {
        return new TrieValueLocator().locate(trie, value);
    }

    @Override
    public Iterator<String> iterator() {
        return TrieIterator.fromBeginning(trie);
    }
    
    public Iterator<String> iterator(long position) {
        return iterator(LargeInteger.of(position));
    }
    
    public Iterator<String> iterator(LargeInteger position) {
        return TrieIterator.fromPosition(trie, position);
    }
    
    
    public static class Builder {
        
        private CharComparator charComparator = null;
        
        private int repeatLimit = AstToTreeConverter.DEFAULT_REPEAT_LIMIT;
        
        private int groupRepeatLimit = AstToTreeConverter.DEFAULT_GROUP_REPEAT_LIMIT;
        
        public Builder charComparator(CharComparator charComparator) {
            this.charComparator = charComparator;
            return this;
        }

        public Builder repeatLimit(int repeatLimit) {
            this.repeatLimit = repeatLimit;
            return this;
        }

        public Builder groupRepeatLimit(int groupRepeatLimit) {
            this.groupRepeatLimit = groupRepeatLimit;
            return this;
        }
        
        public MatchList build(String pattern) {
            return new MatchList(this, pattern);
        }
        
        private CharComparator supplyCharComparator() {
            return charComparator != null ? charComparator : new DefaultCharComparator();
        }
        
    }
    
}
