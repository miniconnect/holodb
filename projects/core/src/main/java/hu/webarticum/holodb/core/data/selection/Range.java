package hu.webarticum.holodb.core.data.selection;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import hu.webarticum.miniconnect.lang.LargeInteger;
import hu.webarticum.miniconnect.lang.ReversibleIterable;

public class Range implements Selection {
    
    private static final Range EMPTY_INSTANCE = empty(LargeInteger.ZERO);
    

    private final LargeInteger from;
    
    private final LargeInteger until;
    
    
    private Range(LargeInteger from, LargeInteger until) {
        this.from = from;
        this.until = until;
    }


    public static Range empty() {
        return EMPTY_INSTANCE;
    }
    
    public static Range empty(long position) {
        return empty(LargeInteger.of(position));
    }
    
    public static Range empty(LargeInteger position) {
        return fromSize(position, LargeInteger.ZERO);
    }
    
    public static Range until(long until) {
        return fromUntil(0, until);
    }

    public static Range until(LargeInteger until) {
        return fromUntil(LargeInteger.ZERO, until);
    }
    
    public static Range fromUntil(long from, long until) {
        return fromUntil(LargeInteger.of(from), LargeInteger.of(until));
    }
    
    public static Range fromUntil(LargeInteger from, LargeInteger until) {
        if (until.compareTo(from) < 0) {
            throw new IllegalArgumentException("Until index can not be lower then from index");
        }
        
        return new Range(from, until);
    }

    public static Range fromSize(long from, long size) {
        return fromSize(LargeInteger.of(from), LargeInteger.of(size));
    }

    public static Range fromSize(LargeInteger from, long size) {
        return fromSize(from, LargeInteger.of(size));
    }
    
    public static Range fromSize(LargeInteger from, LargeInteger size) {
        return fromUntil(from, from.add(size));
    }
    

    public LargeInteger from() {
        return from;
    }

    public LargeInteger until() {
        return until;
    }

    @Override
    public LargeInteger size() {
        return until.subtract(from);
    }

    @Override
    public boolean isEmpty() {
        return until.equals(from);
    }

    @Override
    public LargeInteger at(LargeInteger index) {
        if (index.signum() < 0 || index.compareTo(size()) >= 0) {
            throw new IndexOutOfBoundsException("Index out of bounds: " + index);
        }
        
        return index.add(from);
    }
    
    @Override
    public boolean contains(LargeInteger value) {
        return (value.compareTo(from) >= 0 && value.compareTo(until) < 0);
    }
    
    @Override
    public Iterator<LargeInteger> iterator() {
        return new RangeIterator();
    }

    @Override
    public ReversibleIterable<LargeInteger> reverseOrder() {
        return ReversibleIterable.of(() -> new ReversedRangeIterator(), this);
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
    

    private class RangeIterator implements Iterator<LargeInteger> {

        private LargeInteger counter = from;
        
        private boolean hasNext = checkNext();
        
        
        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public LargeInteger next() {
            if (!hasNext) {
                throw new NoSuchElementException();
            }
            
            LargeInteger result = counter;
            counter = counter.add(LargeInteger.ONE);
            hasNext = checkNext();
            return result;
        }

        private boolean checkNext() {
            return (counter.compareTo(until) < 0);
        }
        
    }

    
    private class ReversedRangeIterator implements Iterator<LargeInteger> {

        private LargeInteger counter = until.subtract(LargeInteger.ONE);
        
        private boolean hasNext = checkNext();
        
        
        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public LargeInteger next() {
            if (!hasNext) {
                throw new NoSuchElementException();
            }
            
            LargeInteger result = counter;
            counter = counter.subtract(LargeInteger.ONE);
            hasNext = checkNext();
            return result;
        }

        private boolean checkNext() {
            return (counter.compareTo(from) >= 0);
        }
        
    }

}
