package hu.webarticum.holodb.core.lab.hasher.performance;

import hu.webarticum.holodb.core.data.hasher.Sha256MacHasher;
import hu.webarticum.holodb.core.util.ByteUtil;
import hu.webarticum.holodb.core.util.ObjectUtil;

class NoPreInitDoubleHasher extends AbstractDemoHasher {

    public NoPreInitDoubleHasher(byte[] key, int hashSize) {
        super(ObjectUtil.apply(new byte[hashSize], data ->
                ByteUtil.fillBytesFrom(data, new Sha256MacHasher().hash(key))));
    }

}