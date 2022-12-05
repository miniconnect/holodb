package hu.webarticum.holodb.core.data.random;

import hu.webarticum.miniconnect.lang.LargeInteger;

public interface TreeRandom {

    public default TreeRandom sub(long number) {
        return sub(LargeInteger.of(number));
    }
    
    public default TreeRandom sub(LargeInteger number) {
        return sub(number.toByteArray());
    }

    public default TreeRandom sub(String name) {
        return sub(name.getBytes());
    }
    
    public TreeRandom sub(byte... bytes);

    public byte[] getBytes(int numberOfBytes);
    
    public LargeInteger getNumber(LargeInteger highExclusive);
    
}
