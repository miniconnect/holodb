package hu.webarticum.holodb.query;

import java.math.BigInteger;

public class QueryLimit {

    private final BigInteger offset;
    
    private final BigInteger limit;
    
    
    public QueryLimit(BigInteger offset, BigInteger limit) {
        this.offset = offset == null ? BigInteger.ZERO : offset;
        this.limit = limit;
    }
    
    
    public BigInteger getOffset() {
        return offset;
    }
    
    public BigInteger getLimit() {
        return limit;
    }
    
}
