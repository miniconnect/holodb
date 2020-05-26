package hu.webarticum.holodb.data.hasher;

import java.util.Arrays;

import hu.webarticum.holodb.util.ByteUtil;

public class FastHasher implements Hasher {

    @Override
    public byte[] hash(byte[] input) {
        return ByteUtil.intToBytes(Arrays.hashCode(input));
    }

}
