package hu.webarticum.holodb.regex.ast.extract;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class ValueExtractor<T extends ExtractableNode<T>> {
    
    private final T extractableNode;

    public ValueExtractor(T extractableNode) {
        this.extractableNode = extractableNode;
    }
    
    public LargeInteger size() {
        return extractableNode.length();
    }
    
    public ImmutableList<Object> get(LargeInteger index) {
        List<Object> resultBuilder = new LinkedList<>();
        getInternal(extractableNode, index, resultBuilder);
        return ImmutableList.fromCollection(resultBuilder);
    }
    
    private void getInternal(T nextNode, LargeInteger index, List<Object> resultBuilder) {
        ImmutableList<T> children = nextNode.children();
        if (children.isEmpty()) {
            return;
        }

        ExtractableValueSet extractableValueSet = nextNode.data();
        LargeInteger[] quotientAndRemainder = index.divideAndRemainder(nextNode.subLength());
        LargeInteger itemIndex = quotientAndRemainder[0];
        LargeInteger subIndex = quotientAndRemainder[1];
        Object nextValue = extractableValueSet.get(itemIndex);
        resultBuilder.add(nextValue);
        LargeInteger remainingIndex = subIndex;
        for (T childNode : children) {
            LargeInteger subLength = childNode.length();
            if (subLength.isGreaterThan(remainingIndex)) {
                getInternal(childNode, remainingIndex, resultBuilder);
                return;
            }
            remainingIndex = remainingIndex.subtract(subLength);
        }
        throw new IndexOutOfBoundsException("Child count: " + nextNode.children().size() + ", index given: " + index);
    }

    public FindResult find(ImmutableList<Object> values) {
        InternalFindResult internalFindResult = findInternal(extractableNode, values.asList());
        boolean found = (internalFindResult.status == InternalFindResult.Status.FOUND);
        return FindResult.of(found, internalFindResult.position);
    }

    public InternalFindResult findInternal(T nextNode, List<Object> values) {
        ExtractableValueSet extractableValueSet = nextNode.data();
        LargeInteger dataSetSize = extractableValueSet.size();
        if (values.isEmpty()) {
            InternalFindResult.Status status = (
                    dataSetSize.isZero() ?
                    InternalFindResult.Status.FOUND :
                    InternalFindResult.Status.BEFORE);
            return new InternalFindResult(status, LargeInteger.ZERO);
        }
        LargeInteger itemLength = nextNode.subLength();
        FindResult setResult = extractableValueSet.find(values.get(0));
        LargeInteger position = setResult.position();
        LargeInteger bottomIndex = position.multiply(itemLength);
        if (!setResult.found()) {
            InternalFindResult.Status status = InternalFindResult.Status.INNER;
            if (position.isZero()) {
                status = InternalFindResult.Status.BEFORE;
            } else if (position.isEqualTo(dataSetSize)) {
                status = InternalFindResult.Status.AFTER;
            }
            return new InternalFindResult(status, bottomIndex);
        }
        for (T childNode : nextNode.children()) {
            List<Object> subList = values.subList(1, values.size());
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
    
    public Comparator<List<Object>> valueListComparator() {
        Comparator<Object> valueComparator = extractableNode.valueComparator();
        return (l1, l2) -> compareLists(valueComparator, l1, l2);
    }
    
    private static int compareLists(Comparator<Object> valueComparator, List<Object> list1, List<Object> list2) {
        Iterator<Object> iterator2 = list2.iterator();
        for (Object value1 : list1) {
            if (!iterator2.hasNext()) {
                return 1;
            }
            Object value2 = iterator2.next();
            int cmp = valueComparator.compare(value1, value2);
            if (cmp != 0) {
                return cmp;
            }
        }
        return iterator2.hasNext() ? -1 : 0;
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
