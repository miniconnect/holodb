package hu.webarticum.holodb.core.data.bitsource;

@FunctionalInterface
public interface ByteSource {

    public byte next();

}
