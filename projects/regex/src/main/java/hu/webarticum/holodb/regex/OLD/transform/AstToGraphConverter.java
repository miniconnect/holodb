package hu.webarticum.holodb.regex.OLD.transform;

import hu.webarticum.holodb.regex.OLD.ast.AlternationAstNode;
import hu.webarticum.holodb.regex.OLD.ast.AstNode;
import hu.webarticum.holodb.regex.OLD.ast.GroupAstNode;
import hu.webarticum.holodb.regex.OLD.ast.QuantifiedAstNode;
import hu.webarticum.holodb.regex.OLD.ast.SequenceAstNode;
import hu.webarticum.holodb.regex.OLD.graph.MutableNode;
import hu.webarticum.holodb.regex.OLD.graph.SpecialValue;
import hu.webarticum.miniconnect.lang.ImmutableList;

public class AstToGraphConverter {

    public MutableNode convert(AstNode astNode) {
        MutableNode node = convert(astNode, new MutableNode(SpecialValue.END));
        if (node.value == null) {
            node.value = SpecialValue.BEGIN;
            return node;
        } else {
            return new MutableNode(SpecialValue.BEGIN, node);
        }
    }

    private MutableNode convert(AstNode astNode, MutableNode... nextNodes) {
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
    
    private MutableNode convertAlternation(AlternationAstNode alternationNode, MutableNode... nextNodes) {
        MutableNode result = new MutableNode();
        result.children.ensureCapacity(alternationNode.branches().size());
        for (SequenceAstNode branch : alternationNode.branches()) {
            result.children.add(convertSequence(branch, nextNodes));
        }
        return result;
    }

    private MutableNode convertGroup(GroupAstNode groupNode, MutableNode... nextNodes) {
        return convertAlternation(groupNode.alternation(), nextNodes);
    }

    private MutableNode convertQuantified(QuantifiedAstNode quantifiedNode, MutableNode... nextNodes) {
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
        MutableNode[] headNodes = nextNodes;
        for (int i = 0; i < optionalOccurrences; i++) {
            headNodes = extend(nextNodes, convert(astNode, headNodes));
        }
        for (int i = 0; i < minOccurrences; i++) {
            headNodes = new MutableNode[] { convert(astNode, headNodes) };
        }
        return join(headNodes);
    }

    private MutableNode convertSequence(SequenceAstNode sequenceNode, MutableNode... nextNodes) {
        ImmutableList<AstNode> nodes = sequenceNode.nodes();
        if (nodes.isEmpty()) {
            return join(nextNodes);
        }
        MutableNode[] headNodes = nextNodes;
        for (AstNode astNode : nodes.reverseOrder()) {
            headNodes = new MutableNode[] { convert(astNode, headNodes) };
        }
        return join(headNodes);
    }

    private MutableNode convertSimple(AstNode simpleNode, MutableNode... nextNodes) {
        return new MutableNode(simpleNode, nextNodes);
    }
    
    private MutableNode join(MutableNode[] nextNodes) {
        if (nextNodes.length == 1) {
            return nextNodes[0];
        } else {
           return new MutableNode(null, nextNodes);
        }
    }
    
    private MutableNode[] extend(MutableNode[] array, MutableNode additional) {
        MutableNode[] result = new MutableNode[array.length + 1];
        System.arraycopy(array, 0, result, 0, array.length);
        result[array.length] = additional;
        return result;
    }
    
}
