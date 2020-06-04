package hu.webarticum.holodb.data.bitsource;

public class ZeroByteSource implements ByteSource {

    @Override
    public byte next() {
        return 0;
    }

}
