package hu.webarticum.holodb.regex.algorithm;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import hu.webarticum.holodb.regex.ast.AnchorAstNode;
import hu.webarticum.holodb.regex.charclass.CharClass;
import hu.webarticum.holodb.regex.tree.TreeNode;
import hu.webarticum.miniconnect.lang.ImmutableList;

public class TreeWeedingTransformer {
    
    public TreeNode weed(TreeNode node) {
        return weedCached(node, null, new HashMap<>()).resultingChildren.get(0);
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
            nextAncestorInfo = new AncestorInfo(ancestorInfo.kind, nextAnchors);
        } else if (value == null) {
            nextAncestorInfo = ancestorInfo;
        } else {
            if (!checkAnchors(ancestorInfo, value)) {
                return new UnlinkResult(true, ImmutableList.empty());
            }
            nextAncestorInfo = new AncestorInfo(kindOf(value), EnumSet.noneOf(AnchorAstNode.class));
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
    
    private CharAnchorKind kindOf(Object value) {
        if (value == SpecialTreeValues.ROOT) {
            return CharAnchorKind.BEGIN;
        } else if (value == SpecialTreeValues.LEAF) {
            return CharAnchorKind.END;
        } else if (!(value instanceof CharClass)) {
            throw new IllegalArgumentException("Non-anchorable value type: " + value);
        }
        CharClass charClass = (CharClass) value;
        String chars = charClass.chars();
        if (chars.isEmpty()) {
            throw new IllegalArgumentException("Empty char class is not anchorable");
        }
        char c = chars.charAt(0);
        return CharAnchorKind.of(c);
    }
    
    private boolean checkAnchors(AncestorInfo ancestorInfo, Object value) {
        if (ancestorInfo == null) {
            return true;
        }
        CharAnchorKind previousKind = ancestorInfo.kind;
        for (AnchorAstNode anchor : ancestorInfo.anchors) {
            if (!checkAnchor(anchor, previousKind, value)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkAnchor(AnchorAstNode anchor, CharAnchorKind previousKind, Object value) {
        switch (anchor) {
            case WORD_BOUNDARY:
                return checkWordBoundaryAnchor(previousKind, value);
            case NON_WORD_BOUNDARY:
                return !checkWordBoundaryAnchor(previousKind, value);
            case BEGIN_OF_LINE:
                return checkBeginOfLineAnchor(previousKind, value);
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
    
    private boolean checkWordBoundaryAnchor(CharAnchorKind previousKind, Object value) {
        boolean previousIsWordChar = previousKind == CharAnchorKind.WORD;
        boolean currentIsWordChar = isWordCharValue(value);
        return currentIsWordChar != previousIsWordChar;
    }
    
    private boolean isWordCharValue(Object value) {
        if (!(value instanceof CharClass)) {
            return false;
        }
        String chars = ((CharClass) value).chars();
        char c = chars.charAt(0);
        return CharAnchorKind.WORD.accept(c);
    }
    
    private boolean checkBeginOfLineAnchor(CharAnchorKind previousKind, Object value) {
        return previousKind == CharAnchorKind.BEGIN || previousKind == CharAnchorKind.NEWLINE;
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
        
        final CharAnchorKind kind;
        
        final EnumSet<AnchorAstNode> anchors;
        
        AncestorInfo(CharAnchorKind kind, EnumSet<AnchorAstNode> anchors) {
            this.kind = kind;
            this.anchors = anchors;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(kind, anchors);
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (!(obj instanceof AncestorInfo)) {
                return false;
            }
            AncestorInfo other = (AncestorInfo) obj;
            return kind == other.kind && Objects.equals(anchors, other.anchors);
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
            if (this == obj) {
                return true;
            } else if (!(obj instanceof CacheKey)) {
                return false;
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
