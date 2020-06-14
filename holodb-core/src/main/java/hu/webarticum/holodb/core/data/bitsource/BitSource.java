package hu.webarticum.holodb.core.data.bitsource;

public interface BitSource {

    public byte[] fetch(int numberOfBits);
    
}
