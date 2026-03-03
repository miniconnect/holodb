package hu.webarticum.holodb.regex.algorithm;

import java.util.ArrayList;

import hu.webarticum.holodb.regex.ast.AlternationAstNode;
import hu.webarticum.holodb.regex.ast.AstNode;
import hu.webarticum.holodb.regex.ast.CharacterConstantAstNode;
import hu.webarticum.holodb.regex.ast.CharacterMatchAstNode;
import hu.webarticum.holodb.regex.ast.FixedStringAstNode;
import hu.webarticum.holodb.regex.ast.GroupAstNode;
import hu.webarticum.holodb.regex.ast.QuantifiedAstNode;
import hu.webarticum.holodb.regex.ast.SequenceAstNode;
import hu.webarticum.holodb.regex.charclass.CharClass;
import hu.webarticum.holodb.regex.comparator.CharComparator;
import hu.webarticum.holodb.regex.tree.TreeNode;
import hu.webarticum.miniconnect.lang.ImmutableList;

public class AstToTreeConverter {

    public static final int DEFAULT_REPEAT_LIMIT = 12;

    public static final int DEFAULT_GROUP_REPEAT_LIMIT = 3;

    private final AstToCharClassesConverter astToCharClassesConverter;

    private final int repeatLimit;

    private final int groupRepeatLimit;

    public AstToTreeConverter(CharComparator charComparator) {
        this(charComparator, DEFAULT_REPEAT_LIMIT, DEFAULT_GROUP_REPEAT_LIMIT);
    }

    public AstToTreeConverter(CharComparator charComparator, int repeatLimit, int groupRepeatLimit) {
        astToCharClassesConverter = new AstToCharClassesConverter(charComparator);
        this.repeatLimit = repeatLimit;
        this.groupRepeatLimit = groupRepeatLimit;
    }

    public TreeNode convert(AlternationAstNode astNode) {
        return convertAlternation(
                astNode,
                SpecialTreeValues.ROOT,
                ImmutableList.of(TreeNode.of(SpecialTreeValues.LEAF)));
    }

    private TreeNode convertAny(AstNode astNode, ImmutableList<TreeNode> nextNodes) {
        if (astNode instanceof AlternationAstNode) {
            return convertAlternation((AlternationAstNode) astNode, nextNodes);
        } else if (astNode instanceof GroupAstNode) {
            return convertGroup((GroupAstNode) astNode, nextNodes);
        } else if (astNode instanceof QuantifiedAstNode) {
            return convertQuantified((QuantifiedAstNode) astNode, nextNodes);
        } else if (astNode instanceof SequenceAstNode) {
            return convertSequence((SequenceAstNode) astNode, nextNodes);
        } else {
            return convertSimple(astNode, nextNodes);
        }
    }

    private TreeNode convertAlternation(AlternationAstNode alternationNode, ImmutableList<TreeNode> nextNodes) {
        return convertAlternation(alternationNode, null, nextNodes);
    }

    private TreeNode convertAlternation(
            AlternationAstNode alternationNode, Object value, ImmutableList<TreeNode> nextNodes) {
        ArrayList<TreeNode> children = new ArrayList<>();
        children.ensureCapacity(alternationNode.branches().size());
        for (SequenceAstNode branch : alternationNode.branches()) {
            children.add(convertSequence(branch, nextNodes));
        }
        return TreeNode.of(value, ImmutableList.fromCollection(children));
    }

    private TreeNode convertGroup(GroupAstNode groupNode, ImmutableList<TreeNode> nextNodes) {
        return convertAlternation(groupNode.alternation(), nextNodes);
    }

    private TreeNode convertQuantified(QuantifiedAstNode quantifiedNode, ImmutableList<TreeNode> nextNodes) {
        int minOccurrences = quantifiedNode.minOccurrences();
        int maxOccurrences = quantifiedNode.maxOccurrences();
        if (minOccurrences == 0 && maxOccurrences == 0) {
            return join(nextNodes);
        }
        AstNode astNode = quantifiedNode.node();
        if (maxOccurrences == QuantifiedAstNode.NO_UPPER_LIMIT) {
            int atLeastMax = (astNode instanceof GroupAstNode) ? groupRepeatLimit : repeatLimit;
            maxOccurrences = Math.max(atLeastMax, minOccurrences);
        }
        int optionalOccurrences = maxOccurrences - minOccurrences;
        ImmutableList<TreeNode> headNodes = nextNodes;
        for (int i = 0; i < optionalOccurrences; i++) {
            headNodes = nextNodes.append(convertAny(astNode, headNodes));
        }
        for (int i = 0; i < minOccurrences; i++) {
            headNodes = ImmutableList.of(convertAny(astNode, headNodes));
        }
        return join(headNodes);
    }

    private TreeNode convertSequence(SequenceAstNode sequenceNode, ImmutableList<TreeNode> nextNodes) {
        ImmutableList<AstNode> nodes = sequenceNode.nodes();
        if (nodes.isEmpty()) {
            return join(nextNodes);
        }
        ImmutableList<TreeNode> headNodes = nextNodes;
        for (AstNode astNode : nodes.reverseOrder()) {
            headNodes = ImmutableList.of(convertAny(astNode, headNodes));
        }
        return join(headNodes);
    }

    private TreeNode join(ImmutableList<TreeNode> nextNodes) {
        if (nextNodes.size() == 1) {
            return nextNodes.get(0);
        } else {
            return TreeNode.of(null, nextNodes);
        }
    }

    private TreeNode convertSimple(AstNode simpleNode, ImmutableList<TreeNode> nextNodes) {
        if (simpleNode instanceof FixedStringAstNode) {
            FixedStringAstNode stringNode = (FixedStringAstNode) simpleNode;
            ImmutableList<TreeNode> headNodes = nextNodes;
            String value = stringNode.value();
            int length = value.length();
            for (int i = 0; i < length; i++) {
                char c = value.charAt(i);
                CharacterConstantAstNode nextCharNode = CharacterConstantAstNode.of(c);
                ImmutableList<CharClass> nextCharClasses = astToCharClassesConverter.convert(nextCharNode);
                ImmutableList<TreeNode> prevHeadNodes = headNodes;
                headNodes = nextCharClasses.map(cc -> TreeNode.of(cc, prevHeadNodes));
            }
            return join(headNodes);
        } else if (simpleNode instanceof CharacterMatchAstNode) {
            CharacterMatchAstNode charNode = (CharacterMatchAstNode) simpleNode;
            ImmutableList<CharClass> charClasses = astToCharClassesConverter.convert(charNode);
            ImmutableList<TreeNode> charNodes = charClasses.map(cc -> TreeNode.of(cc, nextNodes));
            return join(charNodes);
        } else {
            return TreeNode.of(simpleNode, nextNodes);
        }
    }

}
