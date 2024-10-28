package hu.webarticum.holodb.regex.graph.data;

import hu.webarticum.miniconnect.lang.FindPositionResult;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class SortedValueSetNodeData implements NodeData {
    
    private ImmutableList<CharacterValue> values;
    
    public SortedValueSetNodeData(ImmutableList<CharacterValue> values) {
        this.values = values;
    }

    public LargeInteger length() {
        return LargeInteger.of(values.size());
    }
    
    public CharacterValue get(LargeInteger index) {
        return values.get(index.intValue());
    }

    public FindPositionResult find(CharacterValue value) {
        int i = 0;
        for (CharacterValue valueItem : values) {
            int cmp = value.compareTo(valueItem);
            if (cmp == 0) {
                return FindPositionResult.found(LargeInteger.of(i));
            } else if (cmp < 0) {
                return FindPositionResult.notFound(LargeInteger.of(i));
            }
            i++;
        }
        return FindPositionResult.notFound(LargeInteger.of(i));
    }
    
    @Override
    public String toString() {
        return SortedValueSetNodeData.class.getSimpleName() + values.toString();
    }

}
