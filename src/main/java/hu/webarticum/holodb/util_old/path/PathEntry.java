package hu.webarticum.holodb.util_old.path;

import java.math.BigInteger;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class PathEntry {
    
    private final boolean isName;
    
    private final String name;

    private final BigInteger index;
    

    public PathEntry(String name) {
        this.isName = true;
        this.name = name;
        this.index = null;
    }

    public PathEntry(long index) {
        this(BigInteger.valueOf(index));
    }
    
    public PathEntry(BigInteger index) {
        this.isName = false;
        this.name = null;
        this.index = index;
    }
    
    
    public static PathEntry of(Object object) {
        if (object instanceof PathEntry) {
            return (PathEntry) object;
        } else if (object instanceof String) {
            return new PathEntry((String) object);
        } else if (object instanceof BigInteger) {
            return new PathEntry((BigInteger) object);
        } else if (object instanceof Number) {
            return new PathEntry(((Number) object).longValue());
        } else {
            throw new IllegalArgumentException("Bad argument type");
        }
    }

    
    public boolean isName() {
        return isName;
    }

    public BigInteger asIndex() {
        return isName ? BigInteger.valueOf(-1) : index;
    }

    public String asName() {
        return isName ? name : "";
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(isName)
                .append(name)
                .append(index)
                .build();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PathEntry)) {
            return false;
        }
        
        PathEntry otherEntry = (PathEntry) obj;
        if (otherEntry.isName() != isName) {
            return false;
        }
        
        if (isName) {
            return name.equals(otherEntry.asName());
        } else {
            return index.equals(otherEntry.asIndex());
        }
    }
    
    @Override
    public String toString() {
        return isName ? name : index.toString();
    }
    
}
