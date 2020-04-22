package hu.webarticum.holodb.query;

import java.math.BigInteger;
import java.util.Objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class LimitPart {
    
    private final BigInteger from;
    
    private final BigInteger maxCount;
    

    public LimitPart(BigInteger maxCount) {
        this(BigInteger.ZERO, maxCount);
    }

    public LimitPart(BigInteger from, BigInteger maxCount) {
        this.from = from;
        this.maxCount = maxCount;
    }
    

    public BigInteger getFrom() {
        return from;
    }

    public BigInteger getMaxCount() {
        return maxCount;
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(from)
                .append(maxCount)
                .build();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof LimitPart)) {
            return false;
        }
        
        LimitPart other = (LimitPart) obj;
        return
                Objects.equals(from, other.from) &&
                Objects.equals(maxCount, other.maxCount);
    }
    
}
