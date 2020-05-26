package hu.webarticum.holodb.data.random;

import java.math.BigInteger;

public interface TreeRandom {

    public default TreeRandom sub(long number) {
        return sub(BigInteger.valueOf(number));
    }
    
    public default TreeRandom sub(BigInteger number) {
        return sub(number.toByteArray());
    }

    public default TreeRandom sub(String name) {
        return sub(name.getBytes());
    }
    
    public TreeRandom sub(byte... bytes);

    public byte[] getBytes(int numberOfBytes);
    
    public BigInteger getNumber(BigInteger highExclusive);
    
}
