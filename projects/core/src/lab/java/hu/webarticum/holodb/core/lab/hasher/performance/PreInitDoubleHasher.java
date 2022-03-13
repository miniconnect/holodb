package hu.webarticum.holodb.core.lab.hasher.performance;

import hu.webarticum.holodb.core.data.hasher.Hasher;
import hu.webarticum.holodb.core.data.hasher.Sha256MacHasher;
import hu.webarticum.holodb.core.util.ByteUtil;

class PreInitDoubleHasher extends AbstractDemoHasher {

    private static Hasher cachedKeyHasher;
    
    
    public PreInitDoubleHasher(byte[] key, int hashSize) {
        super(buildKey(key, hashSize));
    }

    private static byte[] buildKey(byte[] key, int hashSize) {
        byte[] data = new byte[hashSize];
        ByteUtil.fillBytesFrom(data, getKeyHasher().hash(key));
        return data;
    }
    
    private static synchronized Hasher getKeyHasher() {
        if (cachedKeyHasher == null) {
            cachedKeyHasher = new Sha256MacHasher();
        }
        return cachedKeyHasher;
    }
    
}