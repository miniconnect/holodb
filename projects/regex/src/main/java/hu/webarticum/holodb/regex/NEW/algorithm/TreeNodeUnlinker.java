package hu.webarticum.holodb.regex.NEW.algorithm;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import hu.webarticum.holodb.regex.NEW.tree.TreeNode;
import hu.webarticum.miniconnect.lang.ImmutableList;

public class TreeNodeUnlinker {
    
    private final Predicate<TreeNode> predicate;

    public TreeNodeUnlinker(Predicate<TreeNode> predicate) {
        this.predicate = predicate;
    }
    
    public Predicate<TreeNode> predicate() {
        return predicate;
    }
    
    public ImmutableList<TreeNode> buildUnlinkedOf(TreeNode node) {
        return buildUnlinkedOfInternal(node).resultingChildren;
    }

    public UnlinkResult buildUnlinkedOfInternal(TreeNode node) {
        boolean wasChanged = false;
        ImmutableList<TreeNode> children = node.children();
        int countOfChildren = children.size();
        ImmutableList<TreeNode> firstChangedChildren = null;
        int i = 0;
        while (i < countOfChildren) {
            TreeNode childNode = children.get(i);
            UnlinkResult result = buildUnlinkedOfInternal(childNode);
            if (result.wasChanged) {
                firstChangedChildren = result.resultingChildren;
                break;
            }
            i++;
        }
        ImmutableList<TreeNode> resultChildren = children;
        if (firstChangedChildren != null) {
            wasChanged = true;
            List<TreeNode> resultChildrenListBuilder = new LinkedList<>();
            if (i > 0) {
                resultChildrenListBuilder.addAll(children.asList().subList(0, i));
            }
            resultChildrenListBuilder.addAll(firstChangedChildren.asList());
            i++;
            while (i < countOfChildren) {
                TreeNode childNode = children.get(i);
                UnlinkResult result = buildUnlinkedOfInternal(childNode);
                resultChildrenListBuilder.addAll(result.resultingChildren.asList());
                i++;
            }
            resultChildren = ImmutableList.fromCollection(resultChildrenListBuilder);
        }
        if (predicate.test(node)) {
            return new UnlinkResult(true, resultChildren);
        } else if (!wasChanged) {
            return new UnlinkResult(false, ImmutableList.of(node));
        } else {
            TreeNode newNode = new TreeNode(node.value(), resultChildren);
            return new UnlinkResult(true, ImmutableList.of(newNode));
        }
    }
    
    private static class UnlinkResult {
        
        final boolean wasChanged;
        
        final ImmutableList<TreeNode> resultingChildren;
        
        private UnlinkResult(boolean wasChanged, ImmutableList<TreeNode> resultingChildren)  {
            this.wasChanged = wasChanged;
            this.resultingChildren = resultingChildren;
        }
        
    }
    
}
