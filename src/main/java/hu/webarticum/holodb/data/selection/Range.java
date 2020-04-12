package hu.webarticum.holodb.data.selection;

import java.math.BigInteger;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Range implements Selection {

    private final BigInteger from;
    
    private final BigInteger until;
    
    
    private Range(BigInteger from, BigInteger until) {
        this.from = from;
        this.until = until;
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

    public static Range fromLength(long from, long until) {
        return fromLength(BigInteger.valueOf(from), BigInteger.valueOf(until));
    }

    public static Range fromLength(BigInteger from, long length) {
        return fromLength(from, BigInteger.valueOf(length));
    }
    
    public static Range fromLength(BigInteger from, BigInteger length) {
        return new Range(from, from.add(length));
    }
    

    public BigInteger getFrom() {
        return from;
    }

    public BigInteger getUntil() {
        return until;
    }

    public BigInteger getLength() {
        return until.subtract(from);
    }

    @Override
    public BigInteger getCount() {
        return getLength();
    }

    @Override
    public boolean isEmpty() {
        return getCount().equals(BigInteger.ZERO);
    }

    @Override
    public BigInteger at(BigInteger index) {
        if (index.signum() < 0 || index.compareTo(getCount()) >= 0) {
            throw new IndexOutOfBoundsException("Index out of bounds: " + index);
        }
        
        return index.add(from);
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
