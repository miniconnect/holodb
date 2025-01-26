package hu.webarticum.holodb.regex.NEW.lab;

import hu.webarticum.holodb.regex.NEW.algorithm.AstToTreeConverter;
import hu.webarticum.holodb.regex.NEW.algorithm.TreeToTrieConverter;
import hu.webarticum.holodb.regex.NEW.algorithm.TreeWeedingTransformer;
import hu.webarticum.holodb.regex.NEW.ast.AlternationAstNode;
import hu.webarticum.holodb.regex.NEW.ast.AstNode;
import hu.webarticum.holodb.regex.NEW.comparator.CharComparator;
import hu.webarticum.holodb.regex.NEW.comparator.DefaultCharComparator;
import hu.webarticum.holodb.regex.NEW.parser.RegexParser;
import hu.webarticum.holodb.regex.NEW.tree.TreeNode;
import hu.webarticum.holodb.regex.NEW.trie.TrieNode;
import hu.webarticum.treeprinter.decorator.BorderTreeNodeDecorator;
import hu.webarticum.treeprinter.printer.traditional.TraditionalTreePrinter;

public class AstToTreeDemo {

    public static void main(String[] args) {
        String regex = "x\\d{3}a{1,3}|^[a-f123]([a-c,=]\\b){3,4}$";
        AstNode ast = new RegexParser().parse(regex);
        CharComparator charComparator = new DefaultCharComparator();
        TreeNode tree = new AstToTreeConverter(charComparator).convert((AlternationAstNode) ast);
        TreeNode compactTree = new TreeWeedingTransformer().weed(tree).get(0);
        TrieNode trie = new TreeToTrieConverter(charComparator).convert(compactTree);

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
    }
    
}
