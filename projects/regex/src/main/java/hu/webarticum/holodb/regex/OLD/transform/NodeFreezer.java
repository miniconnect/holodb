package hu.webarticum.holodb.regex.OLD.transform;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

import hu.webarticum.holodb.regex.OLD.ast.extract.ExtractableValueSet;
import hu.webarticum.holodb.regex.OLD.graph.CharacterDataSet;
import hu.webarticum.holodb.regex.OLD.graph.FrozenNode;
import hu.webarticum.holodb.regex.OLD.graph.MutableNode;
import hu.webarticum.holodb.regex.OLD.graph.SpecialValue;
import hu.webarticum.holodb.regex.OLD.graph.SpecialValueDataSet;
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
        ExtractableValueSet extractableValueSet = convertNodeData(mutableNode.value);
        LargeInteger valueCount = extractValueCount(extractableValueSet);
        LargeInteger length = itemLength.multiply(valueCount);
        ImmutableList<FrozenNode> children = ImmutableList.fromCollection(childrenBuilder);
        FrozenNode result = new FrozenNode(extractableValueSet, children, length, itemLength);
        cache.put(mutableNode, result);
        return result;
    }
    
    private ExtractableValueSet convertNodeData(Object value) {
        if (value instanceof ExtractableValueSet) {
            return (ExtractableValueSet) value;
        } else if (value instanceof SpecialValue) {
            return new SpecialValueDataSet((SpecialValue) value);
        } else {
            throw new IllegalArgumentException("Unexpected kind of node data: " + value);
        }
    }

    private LargeInteger extractValueCount(ExtractableValueSet extractableValueSet) {
        if (extractableValueSet instanceof CharacterDataSet) {
            return ((CharacterDataSet) extractableValueSet).size();
        } else {
            return LargeInteger.ONE;
        }
    }
    
}
