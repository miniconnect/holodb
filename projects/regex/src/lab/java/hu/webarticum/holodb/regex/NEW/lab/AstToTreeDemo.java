package hu.webarticum.holodb.regex.NEW.lab;

import hu.webarticum.holodb.regex.NEW.algorithm.AstToTreeConverter;
import hu.webarticum.holodb.regex.NEW.ast.AlternationAstNode;
import hu.webarticum.holodb.regex.NEW.ast.AstNode;
import hu.webarticum.holodb.regex.NEW.parser.RegexParser;
import hu.webarticum.holodb.regex.NEW.tree.TreeNode;
import hu.webarticum.treeprinter.decorator.BorderTreeNodeDecorator;
import hu.webarticum.treeprinter.printer.traditional.TraditionalTreePrinter;

public class AstToTreeDemo {

    public static void main(String[] args) {
        String regex = "x|^[a-c](x|y)$";
        RegexParser parser = new RegexParser();
        AstNode ast = parser.parse(regex);
        AstToTreeConverter converter = new AstToTreeConverter();
        TreeNode tree = converter.convert((AlternationAstNode) ast);

        new TraditionalTreePrinter().print(new BorderTreeNodeDecorator(new AstNodeTreeNode(ast)));
        System.out.println();
        System.out.println("---------------------------------------------------------");
        System.out.println();
        new TraditionalTreePrinter().print(new BorderTreeNodeDecorator(new TreeNodeTreeNode(tree)));
    }
    
}
