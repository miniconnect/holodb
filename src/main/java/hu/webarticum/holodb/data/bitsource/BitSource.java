package hu.webarticum.holodb.data.bitsource;

public interface BitSource {

    public byte[] fetch(int numberOfBits);
    
}
