package hu.webarticum.holodb.util.bitsource;

public class ZeroByteSource implements ByteSource {

    @Override
    public byte next() {
        return 0;
    }

}
