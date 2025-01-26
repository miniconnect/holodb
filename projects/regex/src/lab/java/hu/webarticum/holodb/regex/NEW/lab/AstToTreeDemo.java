package hu.webarticum.holodb.regex.NEW.lab;

import hu.webarticum.holodb.regex.NEW.algorithm.AstToTreeConverter;
import hu.webarticum.holodb.regex.NEW.algorithm.TreeToTrieConverter;
import hu.webarticum.holodb.regex.NEW.algorithm.TreeWeedingTransformer;
import hu.webarticum.holodb.regex.NEW.algorithm.TrieValueRetriever;
import hu.webarticum.holodb.regex.NEW.ast.AlternationAstNode;
import hu.webarticum.holodb.regex.NEW.ast.AstNode;
import hu.webarticum.holodb.regex.NEW.comparator.CharComparator;
import hu.webarticum.holodb.regex.NEW.comparator.DefaultCharComparator;
import hu.webarticum.holodb.regex.NEW.parser.RegexParser;
import hu.webarticum.holodb.regex.NEW.tree.TreeNode;
import hu.webarticum.holodb.regex.NEW.trie.TrieNode;
import hu.webarticum.miniconnect.lang.LargeInteger;
import hu.webarticum.treeprinter.decorator.BorderTreeNodeDecorator;
import hu.webarticum.treeprinter.printer.traditional.TraditionalTreePrinter;

/*

TODO

=== M1 (unordered version): ===
* add retrieving tests for simple, already sorted regular expressions

=== M2 (ordered version): ===
* implement sorting, splitting, and melting branches, using intensive caching
* implement searching
* add a corresponding SortedSource implementation, use this instead of strex (link strex to here)

=== M3 (backreference-aware version): ===
* implement possible-first-character extraction from the referenced groups's ast
* add support for backreference nodes that:
    * are atomic
    * are dynamic-length
    * reference to somewhere in the prefix, can repeat the already determined substring
* handle first-character collision for backreferences (by elimination or shortcut with global error)

=== M4 (basic UCA support): ===
* implement a way to handle secondary/tertiary etc. orders, options:
    * linked trees (as in the paper)
    * dynamic subtree decorators
    * static subtrees (how to handle the danger of memory explosion?)
    * something else

*/

public class AstToTreeDemo {

    public static void main(String[] args) {
        String regex = "x\\d{3}a{1,3}|^[a-f123]([a-c,=]\\b){3,4}$";
        AstNode ast = new RegexParser().parse(regex);
        CharComparator charComparator = new DefaultCharComparator();
        TreeNode tree = new AstToTreeConverter(charComparator).convert((AlternationAstNode) ast);
        TreeNode compactTree = new TreeWeedingTransformer().weed(tree).get(0);
        TrieNode trie = new TreeToTrieConverter(charComparator).convert(compactTree);

        LargeInteger size = trie.size();
        LargeInteger sampleSize = LargeInteger.TEN.min(size);
        LargeInteger stepDiv = LargeInteger.of(37);
        LargeInteger step = size.divide(stepDiv);
        TrieValueRetriever retriever = new TrieValueRetriever();

        new TraditionalTreePrinter().print(new BorderTreeNodeDecorator(new AstNodeTreeNode(ast)));
        System.out.println();
        System.out.println("---------------------------------------------------------");
        System.out.println();
        new TraditionalTreePrinter().print(new BorderTreeNodeDecorator(new TreeNodeTreeNode(tree)));
        System.out.println();
        System.out.println("---------------------------------------------------------");
        System.out.println();
        new TraditionalTreePrinter().print(new BorderTreeNodeDecorator(new TreeNodeTreeNode(compactTree)));
        System.out.println();
        System.out.println("---------------------------------------------------------");
        System.out.println();
        new TraditionalTreePrinter().print(new BorderTreeNodeDecorator(new TrieNodeTreeNode(trie)));
        System.out.println();
        System.out.println("---------------------------------------------------------");
        System.out.println();
        
        System.out.println("Size: " + size);
        for (LargeInteger i = LargeInteger.ZERO; i.isLessThan(sampleSize); i = i.increment()) {
            System.out.println(String.format("%7s: %s", i, retriever.retrieve(trie, i)));
        }
        System.out.println(". . . . . . . . . . .");
        for (LargeInteger i = step; i.isLessThan(size); i = i.add(step)) {
            System.out.println(String.format("%7s: %s", i, retriever.retrieve(trie, i)));
        }
        System.out.println(". . . . . . . . . . .");
        System.out.println(String.format("%7s: %s", size.decrement(), retriever.retrieve(trie, size.decrement())));
    }
    
}
