package hu.webarticum.holodb.storage;

import hu.webarticum.miniconnect.lang.LargeInteger;
import hu.webarticum.miniconnect.rdmsframework.storage.Sequence;

public class HoloFixedSequence implements Sequence {
    
    private final LargeInteger value;
    

    public HoloFixedSequence(LargeInteger value) {
        this.value = value;
    }
    
    
    @Override
    public LargeInteger get() {
        return value;
    }

    @Override
    public LargeInteger getAndIncrement() {
        throw new UnsupportedOperationException("This sequence is read-only");
    }

    @Override
    public void ensureGreaterThan(LargeInteger high) {
        throw new UnsupportedOperationException("This sequence is read-only");
    }

}
