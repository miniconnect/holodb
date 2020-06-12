package hu.webarticum.holodb.demo.hasher.performance;

import hu.webarticum.holodb.util.ByteUtil;
import hu.webarticum.holodb.util.ObjectUtil;

class SingleHasher extends AbstractDemoHasher {
    
    public SingleHasher(byte[] key, int hashSize) {
        super(ObjectUtil.apply(new byte[hashSize], data -> ByteUtil.fillBytesFrom(data, key)));
    }

}