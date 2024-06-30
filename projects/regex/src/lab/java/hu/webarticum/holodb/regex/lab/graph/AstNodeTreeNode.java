package hu.webarticum.holodb.regex.lab.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import hu.webarticum.holodb.regex.ast.AlternationAstNode;
import hu.webarticum.holodb.regex.ast.AstNode;
import hu.webarticum.holodb.regex.ast.GroupAstNode;
import hu.webarticum.holodb.regex.ast.QuantifiedAstNode;
import hu.webarticum.holodb.regex.ast.SequenceAstNode;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.treeprinter.TreeNode;
import hu.webarticum.treeprinter.text.ConsoleText;

public class AstNodeTreeNode implements TreeNode {
    
    private final AstNode astNode;
    
    public AstNodeTreeNode(AstNode astNode) {
        this.astNode = astNode;
    }

    @Override
    public ConsoleText content() {
        StringBuilder labelBuilder = new StringBuilder();
        labelBuilder.append(astNode.getClass().getSimpleName());
        
        // TODO
        
        return ConsoleText.of(labelBuilder.toString());
    }

    @Override
    public List<TreeNode> children() {
        if (astNode instanceof AlternationAstNode) {
            return wrapChildren(((AlternationAstNode) astNode).branches());
        } else if (astNode instanceof GroupAstNode) {
            return wrapOnlyChild(((GroupAstNode) astNode).alternation());
        } else if (astNode instanceof QuantifiedAstNode) {
            return wrapOnlyChild(((QuantifiedAstNode) astNode).node());
        } else if (astNode instanceof SequenceAstNode) {
                return wrapChildren(((SequenceAstNode) astNode).nodes());
        } else {
            return Collections.emptyList();
        }
    }

    private List<TreeNode> wrapOnlyChild(AstNode astNode) {
        return new ArrayList<>(Arrays.asList(new AstNodeTreeNode(astNode)));
    }
    
    private List<TreeNode> wrapChildren(ImmutableList<? extends AstNode> astNodes) {
        return astNodes.map(a -> (TreeNode) new AstNodeTreeNode(a)).toArrayList();
    }

}
