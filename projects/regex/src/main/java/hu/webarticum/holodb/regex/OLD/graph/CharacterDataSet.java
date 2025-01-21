package hu.webarticum.holodb.regex.OLD.graph;

import hu.webarticum.holodb.regex.OLD.ast.extract.ExtractableValueSet;
import hu.webarticum.miniconnect.lang.FindPositionResult;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class CharacterDataSet implements ExtractableValueSet {
    
    private ImmutableList<CharacterValue> values;
    
    public CharacterDataSet(ImmutableList<CharacterValue> values) {
        this.values = values;
    }

    @Override
    public LargeInteger size() {
        return LargeInteger.of(values.size());
    }
    
    @Override
    public CharacterValue get(LargeInteger index) {
        return values.get(index.intValue());
    }

    @Override
    public FindPositionResult find(Object value) {
        if (!(value instanceof CharacterValue)) {
            return FindPositionResult.notFound(LargeInteger.ZERO);
        }
        
        CharacterValue characterValue = (CharacterValue) value;
        int i = 0;
        for (CharacterValue valueItem : values) {
            int cmp = characterValue.compareTo(valueItem);
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
        return CharacterDataSet.class.getSimpleName() + values.toString();
    }

}
