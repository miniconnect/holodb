package hu.webarticum.holodb.regex.NEW.algorithm;

import java.util.ArrayList;

import hu.webarticum.holodb.regex.NEW.ast.AlternationAstNode;
import hu.webarticum.holodb.regex.NEW.ast.AstNode;
import hu.webarticum.holodb.regex.NEW.ast.CharacterConstantAstNode;
import hu.webarticum.holodb.regex.NEW.ast.FixedStringAstNode;
import hu.webarticum.holodb.regex.NEW.ast.GroupAstNode;
import hu.webarticum.holodb.regex.NEW.ast.QuantifiedAstNode;
import hu.webarticum.holodb.regex.NEW.ast.SequenceAstNode;
import hu.webarticum.holodb.regex.NEW.tree.TreeNode;
import hu.webarticum.miniconnect.lang.ImmutableList;

public class AstToTreeConverter {

    public TreeNode convert(AlternationAstNode astNode) {
        return convertAlternation(
                astNode,
                SpecialTreeValues.ROOT,
                ImmutableList.of(new TreeNode(SpecialTreeValues.LEAF)));
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
        return new TreeNode(value, ImmutableList.fromCollection(children));
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
            int atLeastMax = (astNode instanceof GroupAstNode) ? 3 : 12;
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
            return new TreeNode(null, nextNodes);
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
                AstNode nextAstNode = CharacterConstantAstNode.of(c);
                headNodes = ImmutableList.of(convertAny(nextAstNode, headNodes));
            }
            return join(headNodes);
        } else {
            return new TreeNode(simpleNode, nextNodes);
        }
    }

}
