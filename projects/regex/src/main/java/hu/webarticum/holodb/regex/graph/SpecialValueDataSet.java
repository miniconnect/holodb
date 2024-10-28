package hu.webarticum.holodb.regex.graph;

import hu.webarticum.holodb.regex.ast.extract.ExtractableValueSet;
import hu.webarticum.miniconnect.lang.FindPositionResult;
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
        if (index.isNonZero()) {
            throw new IndexOutOfBoundsException("Only zero index is allowed for " + value + ", given index: " + index);
        }
        return value;
    }

    @Override
    public FindPositionResult find(Object value) {
        if (value == this.value) {
            return FindPositionResult.found(LargeInteger.ZERO);
        } else if (value instanceof SpecialValue) {
            boolean fallsBefore = ((SpecialValue) value).ordinal() < this.value.ordinal();
            return FindPositionResult.notFound(fallsBefore ? LargeInteger.ZERO : LargeInteger.ONE);
        } else {
            return FindPositionResult.notFound(LargeInteger.ONE);
        }
    }

}
