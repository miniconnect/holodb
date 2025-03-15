package hu.webarticum.holodb.regex.lab.demo;

import hu.webarticum.holodb.regex.algorithm.AstToTreeConverter;
import hu.webarticum.holodb.regex.algorithm.TreeSortingTransformer;
import hu.webarticum.holodb.regex.algorithm.TreeToTrieConverter;
import hu.webarticum.holodb.regex.algorithm.TreeWeedingTransformer;
import hu.webarticum.holodb.regex.ast.AlternationAstNode;
import hu.webarticum.holodb.regex.comparator.CharComparator;
import hu.webarticum.holodb.regex.comparator.DefaultCharComparator;
import hu.webarticum.holodb.regex.parser.RegexParser;
import hu.webarticum.holodb.regex.tree.TreeNode;
import hu.webarticum.holodb.regex.trie.TrieNode;
import hu.webarticum.holodb.regex.trie.TrieValueRetriever;
import hu.webarticum.miniconnect.lang.LargeInteger;
import hu.webarticum.treeprinter.decorator.BorderTreeNodeDecorator;
import hu.webarticum.treeprinter.printer.traditional.TraditionalTreePrinter;

/*

# TODO

## Back-references

* implement possible-first-character extraction from the referenced groups's ast
* add support for backreference nodes that:
    * are atomic
    * are dynamic-length
    * reference to somewhere in the prefix, can repeat the already determined substring
* handle first-character collision for backreferences (by elimination or shortcut with global error)

## basic UCA support

* implement a way to handle secondary/tertiary etc. orders, options:
    * linked trees (as in the paper)
    * dynamic subtree decorators
    * static subtrees (how to handle the danger of memory explosion?)
    * something else

*/

public class AstToTreeDemo {

    public static void main(String[] args) {
        String regex = "f{0,2}[ra](t[tu]|tue?)s?"; // "x\\d{3}a{1,3}|^[a-f123]([a-c,=]\\b){3,4}$";
        AlternationAstNode ast = (AlternationAstNode) new RegexParser().parse(regex);
        CharComparator charComparator = new DefaultCharComparator();
        TreeNode rawTree = new AstToTreeConverter(charComparator).convert(ast);
        TreeNode compactTree = new TreeWeedingTransformer().weed(rawTree);
        TreeNode sortedTree = new TreeSortingTransformer().sort(compactTree);
        TrieNode trie = new TreeToTrieConverter(charComparator).convert(sortedTree);

        LargeInteger size = trie.size();
        LargeInteger sampleSize = LargeInteger.TEN.min(size);
        LargeInteger stepDiv = LargeInteger.of(37);
        LargeInteger step = size.divide(stepDiv).max(LargeInteger.ONE);
        TrieValueRetriever retriever = new TrieValueRetriever();

        new TraditionalTreePrinter().print(new BorderTreeNodeDecorator(new AstNodeTreeNode(ast)));
        System.out.println();
        System.out.println("---------------------------------------------------------");
        System.out.println();
        new TraditionalTreePrinter().print(new BorderTreeNodeDecorator(new TreeNodeTreeNode(rawTree)));
        System.out.println();
        System.out.println("---------------------------------------------------------");
        System.out.println();
        new TraditionalTreePrinter().print(new BorderTreeNodeDecorator(new TreeNodeTreeNode(compactTree)));
        System.out.println();
        System.out.println("---------------------------------------------------------");
        System.out.println();
        new TraditionalTreePrinter().print(new BorderTreeNodeDecorator(new TreeNodeTreeNode(sortedTree)));
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
