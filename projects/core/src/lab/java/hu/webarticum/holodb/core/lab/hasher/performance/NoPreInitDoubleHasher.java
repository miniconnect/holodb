package hu.webarticum.holodb.core.lab.hasher.performance;

import hu.webarticum.holodb.core.data.hasher.Sha256MacHasher;
import hu.webarticum.holodb.core.util.ByteUtil;

class NoPreInitDoubleHasher extends AbstractDemoHasher {

    public NoPreInitDoubleHasher(byte[] key, int hashSize) {
        super(buildKey(key, hashSize));
    }

    private static byte[] buildKey(byte[] key, int hashSize) {
        byte[] data = new byte[hashSize];
        ByteUtil.fillBytesFrom(data, new Sha256MacHasher().hash(key));
        return data;
    }

}
