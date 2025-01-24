package hu.webarticum.holodb.regex.NEW.algorithm;

import java.util.LinkedList;
import java.util.List;

import hu.webarticum.holodb.regex.NEW.tree.TreeNode;
import hu.webarticum.miniconnect.lang.ImmutableList;

/*

TODO

=== M1 (simple unordered version): ===
* create a rendered character-class class
* eliminate everything that's not a character class, root, nor leaf (e.g., remove anchors)
* implement recursive size calculation
* implement the final tree and its creation for runtime use
* implement the retriever algorithm
* add retrieving tests for simple, pre.ordered regular expressions

=== M2 (anchor-aware, ordered version): ===
* separate character types in the AstToTreeConverter (don't mix digits, letters, and others)
* implement the dead-end case (by throwing an exception, returning an empty list, or something similar)
* implement anchors (with adding parent char to the recursion)
* implement sorting, splitting, and melting branches, using intensive caching
* implement searching
* add a corresponding SortedSource implementation, use this instead of strex (link strex to here)

=== M3 (backreference-aware version): ===
* implement possible-first-character extraction from the referenced groups's ast
* add support for backreference nodes that:
    * are atomic
    * are dynamic-length
    * reference to somewhere in the prefix, can repeat the already determined substring
* handle first-character collision for backreferences (by elimination or shortcut with global error)

=== M4 (basic UCA support): ===
* implement a way to handle secondary/tertiary etc. orders, options:
    * linked trees (as in the paper)
    * dynamic subtree decorators
    * static subtrees (how to handle the danger of memory explosion?)
    * something else

*/

public class TreeCompactionAlgorithm {
    
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
        Object value = node.value();
        if (value == null) {
            return new UnlinkResult(true, resultChildren);
        } else if (!wasChanged) {
            return new UnlinkResult(false, ImmutableList.of(node));
        } else {
            TreeNode newNode = new TreeNode(value, resultChildren);
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
