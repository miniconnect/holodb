package hu.webarticum.holodb.core.lab.hasher.performance;

import hu.webarticum.holodb.core.data.hasher.Hasher;
import hu.webarticum.holodb.core.data.hasher.Sha256MacHasher;
import hu.webarticum.holodb.core.util.ByteUtil;
import hu.webarticum.holodb.core.util.ObjectUtil;

class PreInitDoubleHasher extends AbstractDemoHasher {

    private static Hasher keyHasher;
    
    
    public PreInitDoubleHasher(byte[] key, int hashSize) {
        super(ObjectUtil.apply(new byte[hashSize], data ->
                ByteUtil.fillBytesFrom(data, getKeyHasher().hash(key))));
    }


    private static Hasher getKeyHasher() {
        if (keyHasher == null) {
            keyHasher = new Sha256MacHasher();
        }
        return keyHasher;
    }
    
}