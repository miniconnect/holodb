package hu.webarticum.holodb.regex.ast.extract;

import hu.webarticum.miniconnect.lang.LargeInteger;

public interface ExtractableValueSet {
    
    public LargeInteger size();
    
    public Object get(LargeInteger index);
    
    public FindResult find(Object value);
    
}
