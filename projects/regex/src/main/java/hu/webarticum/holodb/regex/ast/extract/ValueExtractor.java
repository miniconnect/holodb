package hu.webarticum.holodb.regex.ast.extract;

import java.util.LinkedList;
import java.util.List;

import hu.webarticum.holodb.regex.graph.data.CharacterValue;
import hu.webarticum.holodb.regex.graph.data.FindResult;
import hu.webarticum.holodb.regex.graph.data.FlagNodeData;
import hu.webarticum.holodb.regex.graph.data.FrozenNode;
import hu.webarticum.holodb.regex.graph.data.NodeData;
import hu.webarticum.holodb.regex.graph.data.SortedValueSetNodeData;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class ValueExtractor {
    
    private final FrozenNode frozenNode;

    public ValueExtractor(FrozenNode frozenNode) {
        this.frozenNode = frozenNode;
    }
    
    public LargeInteger size() {
        return frozenNode.length();
    }
    
    public ImmutableList<CharacterValue> get(LargeInteger index) {
        List<CharacterValue> resultBuilder = new LinkedList<>();
        getInternal(frozenNode, index, resultBuilder);
        return ImmutableList.fromCollection(resultBuilder);
    }
    
    private void getInternal(FrozenNode nextNode, LargeInteger index, List<CharacterValue> resultBuilder) {
        NodeData nodeData = nextNode.data();
        
        // FIXME
        if (nodeData == FlagNodeData.END) {
            return;
        }
        
        LargeInteger[] quotientAndRemainder = index.divideAndRemainder(nextNode.itemLength());
        LargeInteger itemIndex = quotientAndRemainder[0];
        LargeInteger subIndex = quotientAndRemainder[1];
        if (nodeData instanceof SortedValueSetNodeData) {
            CharacterValue nextValue = ((SortedValueSetNodeData) nodeData).get(itemIndex);
            resultBuilder.add(nextValue);
        }
        LargeInteger remainingIndex = subIndex;
        for (FrozenNode childNode : nextNode.children()) {
            LargeInteger subLength = childNode.length();
            if (subLength.isGreaterThan(remainingIndex)) {
                getInternal(childNode, remainingIndex, resultBuilder);
                return;
            }
            remainingIndex = remainingIndex.subtract(subLength);
        }
        throw new IndexOutOfBoundsException();
    }

    public FindResult find(ImmutableList<CharacterValue> values) {
        InternalFindResult internalFindResult = findInternal(frozenNode, values.asList());
        boolean found = (internalFindResult.status == InternalFindResult.Status.FOUND);
        return FindResult.of(found, internalFindResult.position);
    }

    public InternalFindResult findInternal(FrozenNode nextNode, List<CharacterValue> values) {
        NodeData nodeData = nextNode.data();
        LargeInteger bottomIndex = LargeInteger.ZERO;
        if (nodeData instanceof SortedValueSetNodeData) {
            SortedValueSetNodeData valueSet = (SortedValueSetNodeData) nodeData;
            if (values.isEmpty()) {
                InternalFindResult.Status status = (
                        valueSet.length().isZero() ?
                        InternalFindResult.Status.FOUND :
                        InternalFindResult.Status.BEFORE);
                return new InternalFindResult(status, LargeInteger.ZERO);
            }
            LargeInteger itemLength = nextNode.itemLength();
            FindResult setResult = valueSet.find(values.get(0));
            LargeInteger position = setResult.position();
            bottomIndex = position.multiply(itemLength);
            if (!setResult.found()) {
                InternalFindResult.Status status = InternalFindResult.Status.INNER;
                if (position.isZero()) {
                    status = InternalFindResult.Status.BEFORE;
                } else if (position.isEqualTo(valueSet.length())) {
                    status = InternalFindResult.Status.AFTER;
                }
                return new InternalFindResult(status, bottomIndex);
            }
        }
        for (FrozenNode childNode : nextNode.children()) {
            List<CharacterValue> subList = values.subList(1, values.size());
            InternalFindResult subResult = findInternal(childNode, subList);
            if (subResult.status != InternalFindResult.Status.AFTER) {
                return alignResult(subResult, bottomIndex);
            }
            bottomIndex = bottomIndex.add(childNode.length());
        }
        return new InternalFindResult(InternalFindResult.Status.INNER, bottomIndex);
    }
    
    private InternalFindResult alignResult(InternalFindResult subResult, LargeInteger bottomIndex) {
        InternalFindResult.Status status = (
                subResult.status == InternalFindResult.Status.FOUND ?
                InternalFindResult.Status.FOUND :
                InternalFindResult.Status.INNER);
        LargeInteger position = bottomIndex.add(subResult.position);
        return new InternalFindResult(status, position);
    }

    private static class InternalFindResult {
        
        public enum Status {
            FOUND, INNER, AFTER, BEFORE 
        }
        
        final Status status;
        
        final LargeInteger position;
        
        private InternalFindResult(Status status, LargeInteger position) {
            this.status = status;
            this.position = position;
        }
    
    }
    
}
