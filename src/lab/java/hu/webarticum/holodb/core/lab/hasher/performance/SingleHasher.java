package hu.webarticum.holodb.core.lab.hasher.performance;

import hu.webarticum.holodb.core.util.ByteUtil;
import hu.webarticum.holodb.core.util.ObjectUtil;

class SingleHasher extends AbstractDemoHasher {
    
    public SingleHasher(byte[] key, int hashSize) {
        super(ObjectUtil.apply(new byte[hashSize], data -> ByteUtil.fillBytesFrom(data, key)));
    }

}