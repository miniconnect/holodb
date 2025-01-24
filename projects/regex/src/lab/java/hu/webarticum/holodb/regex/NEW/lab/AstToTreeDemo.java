package hu.webarticum.holodb.regex.NEW.lab;

import hu.webarticum.holodb.regex.NEW.algorithm.AstToTreeConverter;
import hu.webarticum.holodb.regex.NEW.algorithm.TreeNodeUnlinker;
import hu.webarticum.holodb.regex.NEW.ast.AlternationAstNode;
import hu.webarticum.holodb.regex.NEW.ast.AstNode;
import hu.webarticum.holodb.regex.NEW.parser.RegexParser;
import hu.webarticum.holodb.regex.NEW.tree.TreeNode;
import hu.webarticum.treeprinter.decorator.BorderTreeNodeDecorator;
import hu.webarticum.treeprinter.printer.traditional.TraditionalTreePrinter;

public class AstToTreeDemo {

    public static void main(String[] args) {
        String regex = "x\\d{3}a{1,3}|^[a-f123](xxx|yyy)$";
        AstNode ast = new RegexParser().parse(regex);
        TreeNode tree = new AstToTreeConverter().convert((AlternationAstNode) ast);
        TreeNode nullUnlinkedTree = new TreeNodeUnlinker(n -> n.value() == null).buildUnlinkedOf(tree).get(0);

        new TraditionalTreePrinter().print(new BorderTreeNodeDecorator(new AstNodeTreeNode(ast)));
        System.out.println();
        System.out.println("---------------------------------------------------------");
        System.out.println();
        new TraditionalTreePrinter().print(new BorderTreeNodeDecorator(new TreeNodeTreeNode(tree)));
        System.out.println();
        System.out.println("---------------------------------------------------------");
        System.out.println();
        new TraditionalTreePrinter().print(new BorderTreeNodeDecorator(new TreeNodeTreeNode(nullUnlinkedTree)));
    }
    
}
