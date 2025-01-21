package hu.webarticum.holodb.regex.OLD.graph.algorithm;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

import hu.webarticum.holodb.regex.OLD.graph.data.FrozenNode;
import hu.webarticum.holodb.regex.OLD.graph.data.MutableNode;
import hu.webarticum.holodb.regex.OLD.graph.data.NodeData;
import hu.webarticum.holodb.regex.OLD.graph.data.SortedValueSetNodeData;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class NodeFreezer {

    public FrozenNode freeze(MutableNode mutableNode) {
        return freezeInternal(mutableNode, new IdentityHashMap<>());
    }

    private FrozenNode freezeInternal(MutableNode mutableNode, IdentityHashMap<MutableNode, FrozenNode> cache) {
        FrozenNode cachedNode = cache.get(mutableNode);
        if (cachedNode != null) {
            return cachedNode;
        }
        List<FrozenNode> childrenBuilder = new ArrayList<>(mutableNode.children.size());
        LargeInteger itemLength = LargeInteger.ZERO;
        for (MutableNode childMutableNode : mutableNode.children) {
            FrozenNode childFrozenNode = freezeInternal(childMutableNode, cache);
            childrenBuilder.add(childFrozenNode);
            itemLength = itemLength.add(childFrozenNode.length());
        }
        if (mutableNode.children.isEmpty()) {
            itemLength = LargeInteger.ONE;
        }
        NodeData nodeData = checkNodeData(mutableNode.value);
        LargeInteger valueCount = extractValueCount(nodeData);
        LargeInteger length = itemLength.multiply(valueCount);
        ImmutableList<FrozenNode> children = ImmutableList.fromCollection(childrenBuilder);
        FrozenNode result = new FrozenNode(nodeData, children, length, itemLength);
        cache.put(mutableNode, result);
        return result;
    }
    
    private NodeData checkNodeData(Object value) {
        if (!(value instanceof NodeData)) {
            throw new IllegalArgumentException("Not a NodeData: " + value);
        }
        return (NodeData) value;
    }

    private LargeInteger extractValueCount(NodeData nodeData) {
        if (nodeData instanceof SortedValueSetNodeData) {
            return ((SortedValueSetNodeData) nodeData).length();
        } else {
            return LargeInteger.ONE;
        }
    }
    
}
