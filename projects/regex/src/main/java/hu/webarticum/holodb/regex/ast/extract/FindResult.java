package hu.webarticum.holodb.regex.ast.extract;

import hu.webarticum.miniconnect.lang.LargeInteger;

public class FindResult {
    
    private final boolean found;
    
    private final LargeInteger position;
    
    private FindResult(boolean found, LargeInteger position) {
        this.found = found;
        this.position = position;
    }

    public static FindResult of(boolean found, LargeInteger position) {
        return new FindResult(found, position);
    }
    
    public boolean found() {
        return found;
    }
    
    public LargeInteger position() {
        return position;
    }
    
}