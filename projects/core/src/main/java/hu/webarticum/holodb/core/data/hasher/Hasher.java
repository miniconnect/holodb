package hu.webarticum.holodb.core.data.hasher;

@FunctionalInterface
public interface Hasher {

    public byte[] hash(byte[] input);
    
}
