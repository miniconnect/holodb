package hu.webarticum.holodb.data.random;

import java.math.BigInteger;

public interface TreeRandom {

    public TreeRandom sub(byte... bytes);

    public TreeRandom sub(BigInteger number);
    
    public byte getByte();
    
    public byte[] getBytes(int numberOfBytes);
    
    public BigInteger getNumber(BigInteger highExclusive);
    
}
