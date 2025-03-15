package hu.webarticum.holodb.core.data.bitsource;

public class ZeroByteSource implements ByteSource {

    @Override
    public byte next() {
        return 0;
    }

}
