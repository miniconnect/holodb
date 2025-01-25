package hu.webarticum.holodb.regex.NEW.algorithm;

import java.util.LinkedList;
import java.util.List;

import hu.webarticum.holodb.regex.NEW.ast.AnchorAstNode;
import hu.webarticum.holodb.regex.NEW.charclass.CharClass;
import hu.webarticum.holodb.regex.NEW.tree.TreeNode;
import hu.webarticum.miniconnect.lang.ImmutableList;

/*

TODO

=== M1 (simple unordered version): ===
* implement recursive size calculation
* implement the final tree and its creation for runtime use (minimal memory and fast access)
* implement the retriever algorithm
* add retrieving tests for simple, pre.ordered regular expressions

=== M2 (anchor-aware, ordered version): ===
* implement subtree caching
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

public class TreeWeedingAlgorithm {
    
    public ImmutableList<TreeNode> weed(TreeNode node) {
        return weedInternal(node, null).resultingChildren;
    }

    public UnlinkResult weedInternal(TreeNode node, AncestorInfo ancestorInfo) {
        Object value = node.value();
        AncestorInfo nextAncestorInfo;
        if (value instanceof AnchorAstNode) {
            nextAncestorInfo = new AncestorInfo(ancestorInfo.value, ancestorInfo.anchors.append((AnchorAstNode) value));
        } else if (value == null) {
            nextAncestorInfo = ancestorInfo;
        } else {
            if (!checkAnchors(ancestorInfo, value)) {
                return new UnlinkResult(true, ImmutableList.empty());
            }
            nextAncestorInfo = new AncestorInfo(value, ImmutableList.empty());
        }
        ImmutableList<TreeNode> children = node.children();
        int countOfChildren = children.size();
        ImmutableList<TreeNode> firstChangedChildren = null;
        int i = 0;
        while (i < countOfChildren) {
            TreeNode childNode = children.get(i);
            UnlinkResult result = weedInternal(childNode, nextAncestorInfo);
            if (result.wasChanged) {
                firstChangedChildren = result.resultingChildren;
                break;
            }
            i++;
        }
        ImmutableList<TreeNode> resultChildren = children;
        boolean wasChanged = false;
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
                UnlinkResult result = weedInternal(childNode, nextAncestorInfo);
                resultChildrenListBuilder.addAll(result.resultingChildren.asList());
                i++;
            }
            resultChildren = ImmutableList.fromCollection(resultChildrenListBuilder);
        }
        if (
                value == null ||
                value instanceof AnchorAstNode ||
                (value != SpecialTreeValues.LEAF && resultChildren.isEmpty())) {
            return new UnlinkResult(true, resultChildren);
        } else if (!wasChanged) {
            return new UnlinkResult(false, ImmutableList.of(node));
        } else {
            TreeNode newNode = new TreeNode(value, resultChildren);
            return new UnlinkResult(true, ImmutableList.of(newNode));
        }
    }
    
    private boolean checkAnchors(AncestorInfo ancestorInfo, Object value) {
        if (ancestorInfo == null) {
            return true;
        }
        Object previousValue = ancestorInfo.value;
        for (AnchorAstNode anchor : ancestorInfo.anchors) {
            if (!checkAnchor(anchor, previousValue, value)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkAnchor(AnchorAstNode anchor, Object previousValue, Object value) {
        switch (anchor) {
            case WORD_BOUNDARY:
                return checkWordBoundaryAnchor(previousValue, value);
            case NON_WORD_BOUNDARY:
                return !checkWordBoundaryAnchor(previousValue, value);
            case BEGIN_OF_LINE:
                return checkBeginOfLineAnchor(previousValue, value);
            case END_OF_LINE:
                return checkEndOfLineAnchor(previousValue, value);
            case BEGIN_OF_INPUT:
                return checkBeginOfInputAnchor(previousValue, value);
            case END_OF_INPUT:
                return checkEndOfInputAnchor(previousValue, value);
            case END_OF_INPUT_ALLOW_NEWLINE:
                return checkEndOfInputAnchor(previousValue, value); // FIXME
            case END_OF_PREVIOUS_MATCH:
                return true;
            default:
                throw new IllegalArgumentException("Unknown anchor type: " + anchor);
        }
    }
    
    private boolean checkWordBoundaryAnchor(Object previousValue, Object value) {
        return (isAlnumValue(value) != isAlnumValue(previousValue));
    }
    
    private boolean isAlnumValue(Object value) {
        if (!(value instanceof CharClass)) {
            return false;
        }
        String chars = ((CharClass) value).chars();
        char c = chars.charAt(0);
        return Character.isDigit(c) || Character.isAlphabetic(c);
    }
    
    private boolean checkBeginOfLineAnchor(Object previousValue, Object value) {
        if (previousValue == SpecialTreeValues.ROOT) {
            return true;
        } else if (!(value instanceof CharClass)) {
            return false;
        }
        String chars = ((CharClass) previousValue).chars();
        return chars.equals("\n");
    }
    
    private boolean checkEndOfLineAnchor(Object previousValue, Object value) {
        if (value == SpecialTreeValues.LEAF) {
            return true;
        } else if (!(value instanceof CharClass)) {
            return false;
        }
        String chars = ((CharClass) value).chars();
        return chars.equals("\n");
    }
    
    private boolean checkBeginOfInputAnchor(Object previousValue, Object value) {
        return value == SpecialTreeValues.LEAF;
    }
    
    private boolean checkEndOfInputAnchor(Object previousValue, Object value) {
        return value == SpecialTreeValues.LEAF; 
    }
    
    private static class AncestorInfo {
        
        final Object value;
        
        final ImmutableList<AnchorAstNode> anchors;
        
        AncestorInfo(Object value, ImmutableList<AnchorAstNode> anchors) {
            this.value = value;
            this.anchors = anchors;
        }
        
    }
    
    private static class UnlinkResult {
        
        final boolean wasChanged;
        
        final ImmutableList<TreeNode> resultingChildren;
        
        UnlinkResult(boolean wasChanged, ImmutableList<TreeNode> resultingChildren)  {
            this.wasChanged = wasChanged;
            this.resultingChildren = resultingChildren;
        }
        
    }
    
}
