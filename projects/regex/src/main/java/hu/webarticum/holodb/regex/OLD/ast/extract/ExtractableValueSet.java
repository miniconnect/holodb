package hu.webarticum.holodb.regex.OLD.ast.extract;

import hu.webarticum.miniconnect.lang.FindPositionResult;
import hu.webarticum.miniconnect.lang.LargeInteger;

public interface ExtractableValueSet {
    
    public LargeInteger size();
    
    public Object get(LargeInteger index);
    
    public FindPositionResult find(Object value);
    
}
