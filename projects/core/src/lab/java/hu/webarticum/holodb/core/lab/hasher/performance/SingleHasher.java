package hu.webarticum.holodb.core.lab.hasher.performance;

import hu.webarticum.holodb.core.util.ByteUtil;

class SingleHasher extends AbstractDemoHasher {
    
    public SingleHasher(byte[] key, int hashSize) {
        super(buildKey(key, hashSize));
    }

    private static byte[] buildKey(byte[] key, int hashSize) {
        byte[] data = new byte[hashSize];
        ByteUtil.fillBytesFrom(data, key);
        return data;
    }
    
}