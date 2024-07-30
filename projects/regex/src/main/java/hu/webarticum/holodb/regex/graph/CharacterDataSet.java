package hu.webarticum.holodb.regex.graph;

import hu.webarticum.holodb.regex.ast.extract.ExtractableValueSet;
import hu.webarticum.holodb.regex.ast.extract.FindResult;
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
    public FindResult find(Object value) {
        if (!(value instanceof CharacterValue)) {
            return FindResult.of(false, LargeInteger.ZERO);
        }
        
        CharacterValue characterValue = (CharacterValue) value;
        int i = 0;
        for (CharacterValue valueItem : values) {
            int cmp = characterValue.compareTo(valueItem);
            if (cmp == 0) {
                return FindResult.of(true, LargeInteger.of(i));
            } else if (cmp < 0) {
                return FindResult.of(false, LargeInteger.of(i));
            }
            i++;
        }
        return FindResult.of(false, LargeInteger.of(i));
    }
    
    @Override
    public String toString() {
        return CharacterDataSet.class.getSimpleName() + values.toString();
    }

}
