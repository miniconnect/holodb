package hu.webarticum.holodb.regex.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import hu.webarticum.holodb.regex.charclass.CharClass;
import hu.webarticum.holodb.regex.charclass.CharClassEntrySetSplitter;
import hu.webarticum.holodb.regex.charclass.SimpleEntryList;
import hu.webarticum.holodb.regex.charclass.SimpleEntryList.Entry;
import hu.webarticum.holodb.regex.comparator.CharComparator;
import hu.webarticum.holodb.regex.tree.TreeNode;
import hu.webarticum.miniconnect.lang.ImmutableList;

public class TreeSortingTransformer {

    private final CharClassEntrySetSplitter<TreeNode> splitter = new CharClassEntrySetSplitter<>();

    public TreeNode sort(TreeNode node) {
        return sortCached(node, new IdentityHashMap<>());
    }

    private TreeNode sortCached(TreeNode node, Map<TreeNode, TreeNode> cache) {
        TreeNode cachedResult = cache.get(node);
        if (cachedResult != null) {
            return cachedResult;
        }
        TreeNode transformedNode = sortInternal(node, cache);
        cache.put(node, transformedNode);
        return transformedNode;
    }

    private TreeNode sortInternal(TreeNode node, Map<TreeNode, TreeNode> cache) {
        Object value = node.value();
        if (value == SpecialTreeValues.LEAF) {
            return node;
        }
        ImmutableList<TreeNode> children = node.children();
        TransformResult result = sortChildren(children, cache);
        if (result.wasChanged) {
            return TreeNode.of(value, result.resultingChildren);
        } else {
            return node;
        }
    }

    private TransformResult sortChildren(ImmutableList<TreeNode> children, Map<TreeNode, TreeNode> cache) {
        SimpleEntryList<CharClass, TreeNode> entries = new SimpleEntryList<>();
        List<TreeNode> newChildrenBuilder = new ArrayList<>(children.size());
        boolean wasChanged = false;
        CharClass previousCharClass = null;
        for (TreeNode childNode : children) {
            Object childValue = childNode.value();
            if (childValue instanceof CharClass) {
                CharClass charClass = (CharClass) childValue;
                entries.add(charClass, childNode);
                if (!areInOrder(previousCharClass, charClass)) {
                    wasChanged = true;
                }
                previousCharClass = charClass;
            } else {
                if (!entries.isEmpty() || !newChildrenBuilder.isEmpty()) {
                    wasChanged = true;
                }
                newChildrenBuilder.add(childNode);
            }
        }
        SimpleEntryList<CharClass, ImmutableList<TreeNode>> splittedEntries = splitter.split(entries);
        for (SimpleEntryList.Entry<CharClass, ImmutableList<TreeNode>> splittedEntry : splittedEntries) {
            boolean entryWasChanged = transformSplittedEntry(splittedEntry, newChildrenBuilder, cache);
            wasChanged = wasChanged || entryWasChanged;
        }
        if (wasChanged) {
            ImmutableList<TreeNode> newChildren = ImmutableList.fromCollection(newChildrenBuilder);
            return new TransformResult(true, newChildren);
        } else {
            return new TransformResult(false, children);
        }
    }

    private boolean areInOrder(CharClass previousCharClass, CharClass charClass) {
        if (previousCharClass == null) {
            return true;
        }
        CharComparator comparator = previousCharClass.charComparator();
        char previousC = previousCharClass.chars().charAt(0);
        char c = charClass.chars().charAt(0);
        return comparator.compare(previousC, c) < 0;
    }

    private boolean transformSplittedEntry(
            Entry<CharClass, ImmutableList<TreeNode>> splittedEntry,
            List<TreeNode> newChildrenBuilder,
            Map<TreeNode, TreeNode> cache) {
        if (isSingleBranch(splittedEntry)) {
            return handleSingleBranch(splittedEntry.value().get(0), newChildrenBuilder, cache);
        } else {
            return handleSplittingBranches(splittedEntry, newChildrenBuilder, cache);
        }
    }

    private boolean isSingleBranch(Entry<CharClass, ImmutableList<TreeNode>> splittedEntry) {
        ImmutableList<TreeNode> splittedChildren = splittedEntry.value();
        if (splittedChildren.size() != 1) {
            return false;
        }
        TreeNode soleChild = splittedChildren.get(0);
        CharClass childCharClass = (CharClass) soleChild.value();
        CharClass entryCharClass = splittedEntry.key();
        return childCharClass.equals(entryCharClass);
    }

    private boolean handleSingleBranch(
            TreeNode originalNode, List<TreeNode> newChildrenBuilder, Map<TreeNode, TreeNode> cache) {
        TreeNode transformedNode = sortCached(originalNode, cache);
        newChildrenBuilder.add(transformedNode);
        return transformedNode != originalNode;
    }

    private boolean handleSplittingBranches(
            Entry<CharClass, ImmutableList<TreeNode>> splittedEntry,
            List<TreeNode> newChildrenBuilder,
            Map<TreeNode, TreeNode> cache) {
        CharClass charClass = splittedEntry.key();
        ImmutableList<TreeNode> splittedChildren = splittedEntry.value();
        ImmutableList<TreeNode> deduplicatedSubChildren = deduplicateSub(splittedChildren);
        ImmutableList<TreeNode> sortedSubChildren = sortChildren(deduplicatedSubChildren, cache).resultingChildren;
        TreeNode newChildNode = TreeNode.of(charClass, sortedSubChildren);
        newChildrenBuilder.add(newChildNode);
        return true;
    }

    private ImmutableList<TreeNode> deduplicateSub(ImmutableList<TreeNode> splittedChildren) {
        Set<TreeNode> subChildReferences = Collections.newSetFromMap(new IdentityHashMap<TreeNode, Boolean>());
        for (TreeNode childNode : splittedChildren) {
            subChildReferences.addAll(childNode.children().asList());
        }
        return ImmutableList.fromCollection(subChildReferences);
    }

}
