package hu.webarticum.holodb.regex.NEW.algorithm;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
        return weedCached(node, null, new HashMap<>()).resultingChildren;
    }
    
    private UnlinkResult weedCached(
            TreeNode node, AncestorInfo ancestorInfo, Map<CacheKey, ImmutableList<TreeNode>> cache) {
        Object value = node.value();
        if (!(value instanceof CharClass) && value != SpecialTreeValues.LEAF) {
            return weedInternal(node, ancestorInfo, cache);
        }
        CacheKey cacheKey = createCacheKey(node, ancestorInfo);
        ImmutableList<TreeNode> cachedChildren = cache.get(cacheKey);
        if (cachedChildren != null) {
            boolean wasChanged = (cachedChildren.size() != 1) || cachedChildren.get(0) != node;
            return new UnlinkResult(wasChanged, cachedChildren);
        }
        UnlinkResult result = weedInternal(node, ancestorInfo, cache);
        cache.put(cacheKey, result.resultingChildren);
        return result;
    }
    
    private CacheKey createCacheKey(TreeNode node, AncestorInfo ancestorInfo) {
        AncestorInfo normalizedAncestorInfo = ancestorInfo;
        if (ancestorInfo == null || ancestorInfo.anchors.isEmpty()) {
            normalizedAncestorInfo = null;
        }
        return new CacheKey(node, normalizedAncestorInfo);
    }

    private UnlinkResult weedInternal(
            TreeNode node, AncestorInfo ancestorInfo, Map<CacheKey, ImmutableList<TreeNode>> cache) {
        Object value = node.value();
        AncestorInfo nextAncestorInfo;
        if (value instanceof AnchorAstNode) {
            AnchorAstNode achorValue = (AnchorAstNode) value;
            EnumSet<AnchorAstNode> nextAnchors = EnumSet.copyOf(ancestorInfo.anchors);
            nextAnchors.add(achorValue);
            nextAncestorInfo = new AncestorInfo(ancestorInfo.value, nextAnchors);
        } else if (value == null) {
            nextAncestorInfo = ancestorInfo;
        } else {
            if (!checkAnchors(ancestorInfo, value)) {
                return new UnlinkResult(true, ImmutableList.empty());
            }
            nextAncestorInfo = new AncestorInfo(value, EnumSet.noneOf(AnchorAstNode.class));
        }
        ImmutableList<TreeNode> children = node.children();
        int countOfChildren = children.size();
        ImmutableList<TreeNode> firstChangedChildren = null;
        int i = 0;
        while (i < countOfChildren) {
            TreeNode childNode = children.get(i);
            UnlinkResult result = weedCached(childNode, nextAncestorInfo, cache);
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
                UnlinkResult result = weedCached(childNode, nextAncestorInfo, cache);
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
                return checkEndOfLineAnchor(value);
            case BEGIN_OF_INPUT:
                return checkBeginOfInputAnchor(value);
            case END_OF_INPUT:
                return checkEndOfInputAnchor(value);
            case END_OF_INPUT_ALLOW_NEWLINE:
                return checkEndOfInputAnchor(value); // FIXME
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
    
    private boolean checkEndOfLineAnchor(Object value) {
        if (value == SpecialTreeValues.LEAF) {
            return true;
        } else if (!(value instanceof CharClass)) {
            return false;
        }
        String chars = ((CharClass) value).chars();
        return chars.equals("\n");
    }
    
    private boolean checkBeginOfInputAnchor(Object value) {
        return value == SpecialTreeValues.LEAF;
    }
    
    private boolean checkEndOfInputAnchor(Object value) {
        return value == SpecialTreeValues.LEAF; 
    }
    
    private static class AncestorInfo {
        
        // TODO: it's enough to store the kind of the node value
        final Object value;
        
        final EnumSet<AnchorAstNode> anchors;
        
        AncestorInfo(Object value, EnumSet<AnchorAstNode> anchors) {
            this.value = value;
            this.anchors = anchors;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(value, anchors);
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof AncestorInfo)) {
                return false;
            } else if (this == obj) {
                return true;
            }
            AncestorInfo other = (AncestorInfo) obj;
            return Objects.equals(value, other.value) && Objects.equals(anchors, other.anchors);
        }
        
    }
    
    private static class CacheKey {
        
        final TreeNode treeNode;
        
        final AncestorInfo ancestorInfo;
        
        public CacheKey(TreeNode treeNode, AncestorInfo ancestorInfo) {
            this.treeNode = treeNode;
            this.ancestorInfo = ancestorInfo;
        }
        
        @Override
        public int hashCode() {
            return (System.identityHashCode(treeNode) * 31) + Objects.hashCode(ancestorInfo);
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof CacheKey)) {
                return false;
            } else if (this == obj) {
                return true;
            }
            CacheKey other = (CacheKey) obj;
            return (treeNode == other.treeNode) && Objects.equals(ancestorInfo, other.ancestorInfo);
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
