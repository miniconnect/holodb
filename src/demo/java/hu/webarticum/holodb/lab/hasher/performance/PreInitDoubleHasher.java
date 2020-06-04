package hu.webarticum.holodb.lab.hasher.performance;

import hu.webarticum.holodb.data.hasher.Hasher;
import hu.webarticum.holodb.data.hasher.Sha256MacHasher;
import hu.webarticum.holodb.util.ByteUtil;
import hu.webarticum.holodb.util.ObjectUtil;

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