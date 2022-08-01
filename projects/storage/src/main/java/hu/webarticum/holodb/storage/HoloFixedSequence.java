package hu.webarticum.holodb.storage;

import java.math.BigInteger;

import hu.webarticum.miniconnect.rdmsframework.storage.Sequence;

public class HoloFixedSequence implements Sequence {
    
    private final BigInteger value;
    

    public HoloFixedSequence(BigInteger value) {
        this.value = value;
    }
    
    
    @Override
    public BigInteger get() {
        return value;
    }

    @Override
    public BigInteger getAndIncrement() {
        throw new UnsupportedOperationException("This sequence is read-only");
    }

    @Override
    public void ensureGreaterThan(BigInteger high) {
        throw new UnsupportedOperationException("This sequence is read-only");
    }

}
