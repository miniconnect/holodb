package hu.webarticum.holodb.core.data.selection;

import java.math.BigInteger;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Range implements Selection {

    private final BigInteger from;
    
    private final BigInteger until;
    
    
    private Range(BigInteger from, BigInteger until) {
        this.from = from;
        this.until = until;
    }

    
    public static Range empty(long position) {
        return empty(BigInteger.valueOf(position));
    }
    
    public static Range empty(BigInteger position) {
        return fromSize(position, BigInteger.ZERO);
    }
    
    public static Range until(long until) {
        return fromUntil(0, until);
    }

    public static Range until(BigInteger until) {
        return fromUntil(BigInteger.ZERO, until);
    }
    
    public static Range fromUntil(long from, long until) {
        return fromUntil(BigInteger.valueOf(from), BigInteger.valueOf(until));
    }
    
    public static Range fromUntil(BigInteger from, BigInteger until) {
        if (until.compareTo(from) < 0) {
            throw new IllegalArgumentException("Until index can not be lower then from index");
        }
        
        return new Range(from, until);
    }

    public static Range fromSize(long from, long size) {
        return fromSize(BigInteger.valueOf(from), BigInteger.valueOf(size));
    }

    public static Range fromSize(BigInteger from, long size) {
        return fromSize(from, BigInteger.valueOf(size));
    }
    
    public static Range fromSize(BigInteger from, BigInteger size) {
        return fromUntil(from, from.add(size));
    }
    

    public BigInteger from() {
        return from;
    }

    public BigInteger until() {
        return until;
    }

    @Override
    public BigInteger size() {
        return until.subtract(from);
    }

    @Override
    public boolean isEmpty() {
        return until.equals(from);
    }

    @Override
    public BigInteger at(BigInteger index) {
        if (index.signum() < 0 || index.compareTo(size()) >= 0) {
            throw new IndexOutOfBoundsException("Index out of bounds: " + index);
        }
        
        return index.add(from);
    }
    
    @Override
    public boolean contains(BigInteger value) {
        return (value.compareTo(from) >= 0 && value.compareTo(until) < 0);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Range)) {
            return false;
        }
        
        Range otherRange = (Range) obj;
        return from.equals(otherRange.from) && until.equals(otherRange.until);
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(from).append(until).toHashCode();
    }
    
    @Override
    public String toString() {
        return String.format("[%d, %d)", from, until);
    }
    
}
