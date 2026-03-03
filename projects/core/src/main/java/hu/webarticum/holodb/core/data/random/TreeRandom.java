package hu.webarticum.holodb.core.data.random;

import java.nio.charset.StandardCharsets;

import hu.webarticum.miniconnect.lang.LargeInteger;

public interface TreeRandom {

    public default TreeRandom sub(boolean key) {
        return sub(new byte[] { (key ? (byte) 1 : (byte) 0) });
    }

    public default TreeRandom sub(long key) {
        return sub(LargeInteger.of(key));
    }

    public default TreeRandom sub(LargeInteger key) {
        return sub(key.toByteArray());
    }

    public default TreeRandom sub(String key) {
        return sub(key.getBytes(StandardCharsets.UTF_8));
    }

    public TreeRandom sub(byte[] key);

    public byte[] getBytes(int numberOfBytes);

    public LargeInteger getNumber(LargeInteger highExclusive);

}
