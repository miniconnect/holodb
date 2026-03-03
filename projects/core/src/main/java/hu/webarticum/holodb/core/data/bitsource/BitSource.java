package hu.webarticum.holodb.core.data.bitsource;

@FunctionalInterface
public interface BitSource {

    public byte[] fetch(int numberOfBits);

}
