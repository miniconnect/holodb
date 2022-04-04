package hu.webarticum.holodb.core.data.source;

import java.math.BigInteger;

import hu.webarticum.holodb.core.data.selection.Range;

public class RangeSource implements SortedSource<BigInteger> {
    
    private final BigInteger from;
    
    private final BigInteger size;
    
    private final BigInteger step;
    

    public RangeSource(BigInteger size) {
        this(BigInteger.ZERO, size);
    }
    
    public RangeSource(BigInteger from, BigInteger size) {
        this(from, size, BigInteger.ONE);
    }
    
    public RangeSource(BigInteger from, BigInteger size, BigInteger step) {
        if (step.signum() != 1) {
            throw new IllegalArgumentException("step must be positive");
        }
        
        this.from = from;
        this.size = size;
        this.step = step;
    }


    @Override
    public Class<BigInteger> type() {
        return BigInteger.class;
    }


    @Override
    public BigInteger size() {
        return size;
    }


    @Override
    public BigInteger get(BigInteger index) {
        return from.add(step.multiply(index));
    }


    @Override
    public Range find(Object value) {
        
        // TODO
        return null;
        
    }


    @Override
    public Range findBetween(
            Object minValue, boolean minInclusive, Object maxValue, boolean maxInclusive) {
        
        // TODO
        return null;
        
    }
    
}
