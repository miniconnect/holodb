package hu.webarticum.holodb.demo.hasher.performance;

import hu.webarticum.holodb.data.hasher.Sha256MacHasher;
import hu.webarticum.holodb.util.ByteUtil;
import hu.webarticum.holodb.util.ObjectUtil;

class NoPreInitDoubleHasher extends AbstractDemoHasher {

    public NoPreInitDoubleHasher(byte[] key, int hashSize) {
        super(ObjectUtil.apply(new byte[hashSize], data ->
                ByteUtil.fillBytesFrom(data, new Sha256MacHasher().hash(key))));
    }

}