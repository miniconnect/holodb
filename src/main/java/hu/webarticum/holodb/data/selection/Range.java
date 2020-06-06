package hu.webarticum.holodb.data.selection;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Range implements Selection {

    private final BigInteger from;
    
    private final BigInteger until;
    
    
    private Range(BigInteger from, BigInteger until) {
        this.from = from;
        this.until = until;
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

    public static Range fromLength(long from, long until) {
        return fromLength(BigInteger.valueOf(from), BigInteger.valueOf(until));
    }

    public static Range fromLength(BigInteger from, long length) {
        return fromLength(from, BigInteger.valueOf(length));
    }
    
    public static Range fromLength(BigInteger from, BigInteger length) {
        return fromUntil(from, from.add(length));
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
        return until.equals(from);
    }

    @Override
    public BigInteger at(BigInteger index) {
        if (index.signum() < 0 || index.compareTo(getCount()) >= 0) {
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
    
    @Override
    public Iterator<BigInteger> iterator() {
        return new RangeIterator();
    }
    
    
    private class RangeIterator implements Iterator<BigInteger> {

        private BigInteger counter = from;
        
        
        @Override
        public boolean hasNext() {
            return (counter.compareTo(until) < 0);
        }

        @Override
        public BigInteger next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            
            BigInteger result = counter;
            counter = counter.add(BigInteger.ONE);
            return result;
        }
        
    }
    
}
