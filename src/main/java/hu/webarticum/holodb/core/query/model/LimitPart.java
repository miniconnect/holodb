package hu.webarticum.holodb.core.query.model;

import java.math.BigInteger;
import java.util.Objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public final class LimitPart {
    
    private final BigInteger from;
    
    private final BigInteger maxCount;
    

    public LimitPart(BigInteger maxCount) {
        this(BigInteger.ZERO, maxCount);
    }

    public LimitPart(BigInteger from, BigInteger maxCount) {
        this.from = Objects.requireNonNull(from, "From can not be null");
        
        if (maxCount != null && maxCount.signum() == -1) {
            throw new IllegalArgumentException("Max count can not be negative");
        }
        this.maxCount = maxCount;
    }
    

    public BigInteger getFrom() {
        return from;
    }

    public boolean hasMaxCount() {
        return (maxCount != null);
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
