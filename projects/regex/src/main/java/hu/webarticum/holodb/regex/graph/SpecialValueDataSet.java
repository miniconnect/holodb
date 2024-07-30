package hu.webarticum.holodb.regex.graph;

import hu.webarticum.holodb.regex.ast.extract.ExtractableValueSet;
import hu.webarticum.holodb.regex.ast.extract.FindResult;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class SpecialValueDataSet implements ExtractableValueSet {
    
    private final SpecialValue value;
    
    public SpecialValueDataSet(SpecialValue value) {
        this.value = value;
    }

    @Override
    public LargeInteger size() {
        return LargeInteger.ONE;
    }

    @Override
    public Object get(LargeInteger index) {
        if (index != LargeInteger.ZERO) {
            throw new IndexOutOfBoundsException();
        }
        return value;
    }

    @Override
    public FindResult find(Object value) {
        if (value == this.value) {
            return FindResult.of(true, LargeInteger.ZERO);
        } else if (value instanceof SpecialValue) {
            boolean fallsBefore = ((SpecialValue) value).ordinal() < this.value.ordinal();
            return FindResult.of(false, fallsBefore ? LargeInteger.ZERO : LargeInteger.ONE);
        } else {
            return FindResult.of(false, LargeInteger.ONE);
        }
    }

}
