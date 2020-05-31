package hu.webarticum.holodb.data.hasher;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

import hu.webarticum.holodb.util.ByteUtil;

public class FastHasher implements Hasher {

    // FIXME: best implementation?
    @Override
    public byte[] hash(byte[] input) {
        byte[] result = ByteUtil.intToBytes(Arrays.hashCode(input));
        ArrayUtils.reverse(result);
        return result;
    }

}
