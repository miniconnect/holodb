package hu.webarticum.holodb.regex.lab.graph;

import java.util.stream.Collectors;

import hu.webarticum.holodb.regex.ast.AstNode;
import hu.webarticum.holodb.regex.ast.extract.ValueExtractor;
import hu.webarticum.holodb.regex.graph.CharacterValue;
import hu.webarticum.holodb.regex.graph.FrozenNode;
import hu.webarticum.holodb.regex.graph.MutableNode;
import hu.webarticum.holodb.regex.parser.RegexParser;
import hu.webarticum.holodb.regex.transform.AstToGraphConverter;
import hu.webarticum.holodb.regex.transform.GraphCharacterTransformer;
import hu.webarticum.holodb.regex.transform.NodeFreezer;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;
import hu.webarticum.treeprinter.decorator.BorderTreeNodeDecorator;
import hu.webarticum.treeprinter.printer.traditional.TraditionalTreePrinter;

public class TestStringGeneratorMain {

    public static void main(String[] args) {
        //String regex = "(a|bc?){2}x{3,4}";
        String regex = "\\d{3}\\-\\d{4}(\\-(ABC|XXX)){3}";
        
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
        
        ValueExtractor<FrozenNode> extractor = new ValueExtractor<>(frozenGraph);

        System.out.println("\n------------------------\n");
        System.out.println("Size: " + extractor.size());
        System.out.println();
        int end = extractor.size().min(LargeInteger.of(100)).intValue();
        for (int i = 0; i < end; i++) {
            String value = extractAt(extractor, LargeInteger.of(i));
            System.out.println("Value " + i + ": " + value);
        }
        if (LargeInteger.of(end).isLessThan(extractor.size())) {
            System.out.println("[...]");
        }

        System.out.println("\n------------------------\n");
        LargeInteger someIndex = extractor.size().divide(LargeInteger.of(17))
                .add( extractor.size().divide(LargeInteger.of(11)));
        String someValue = extractAt(extractor, someIndex);
        System.out.println("Some value " + someIndex + ": " + someValue);
    }
    
    private static String extractAt(ValueExtractor<FrozenNode> extractor, LargeInteger index) {
        ImmutableList<Object> valueList = extractor.get(index);
        return stringify(valueList);
    }
    
    private static String stringify(ImmutableList<Object> valueList) {
        return valueList.stream()
                .filter(CharacterValue.class::isInstance).map(v -> (CharacterValue) v)
                .map(v -> Character.toString(v.value()))
                .collect(Collectors.joining(""));
    }
    
}
