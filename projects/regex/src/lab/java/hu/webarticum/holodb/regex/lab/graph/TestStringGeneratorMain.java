package hu.webarticum.holodb.regex.lab.graph;

import hu.webarticum.holodb.regex.ast.AstNode;
import hu.webarticum.holodb.regex.ast.extract.ValueExtractor;
import hu.webarticum.holodb.regex.graph.algorithm.AstToGraphConverter;
import hu.webarticum.holodb.regex.graph.algorithm.GraphCharacterTransformer;
import hu.webarticum.holodb.regex.graph.algorithm.NodeFreezer;
import hu.webarticum.holodb.regex.graph.data.CharacterValue;
import hu.webarticum.holodb.regex.graph.data.FrozenNode;
import hu.webarticum.holodb.regex.graph.data.MutableNode;
import hu.webarticum.holodb.regex.parser.RegexParser;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;
import hu.webarticum.treeprinter.decorator.BorderTreeNodeDecorator;
import hu.webarticum.treeprinter.printer.traditional.TraditionalTreePrinter;

public class TestStringGeneratorMain {

    public static void main(String[] args) {
        String regex = "(a|bc?){2}x{3,4}";
        System.out.println("Regex: " + regex);
        
        AstNode astNode = new RegexParser().parse(regex);
        System.out.println("\n------------------------\n");
        System.out.println(astNode);

        System.out.println("\n------------------------\n");
        new TraditionalTreePrinter().print(new BorderTreeNodeDecorator(new AstNodeTreeNode(astNode)));
        
        MutableNode mutableGraph = new AstToGraphConverter().convert(astNode);
        System.out.println("\n------------------------\n");
        new TraditionalTreePrinter().print(new BorderTreeNodeDecorator(new MutableNodeTreeNode(mutableGraph)));
        
        new GraphCharacterTransformer().transform(mutableGraph);
        System.out.println("\n------------------------\n");
        new TraditionalTreePrinter().print(new BorderTreeNodeDecorator(new MutableNodeTreeNode(mutableGraph)));
        
        FrozenNode frozenGraph = new NodeFreezer().freeze(mutableGraph);
        System.out.println("\n------------------------\n");
        new TraditionalTreePrinter().print(new BorderTreeNodeDecorator(new FrozenNodeTreeNode(frozenGraph)));
        
        ValueExtractor extractor = new ValueExtractor(frozenGraph);

        System.out.println("\n------------------------\n");
        System.out.println("Size: " + extractor.size());
        System.out.println();
        int end = extractor.size().min(LargeInteger.of(100)).intValue();
        for (int i = 0; i < end; i++) {
            ImmutableList<CharacterValue> valueList = extractor.get(LargeInteger.of(i));
            String value = String.join("", valueList.map(v -> Character.toString(v.value())));
            System.out.println("Value " + i + ": " + value);
        }
        if (LargeInteger.of(end).isLessThan(extractor.size())) {
            System.out.println("[...]");
        }
    }
    
}
