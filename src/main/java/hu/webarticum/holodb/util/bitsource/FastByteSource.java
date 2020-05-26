package hu.webarticum.holodb.util.bitsource;

import hu.webarticum.holodb.util.ByteUtil;

public class FastByteSource implements ByteSource {

    private static int STEP = 17 * 31 * 43;
    
    
    private byte stateByte;
    

    public FastByteSource() {
        this((byte) 0);
    }

    public FastByteSource(byte seed) {
        this.stateByte = seed;
    }
    
    
    @Override
    public byte next() {
        stateByte = (byte) ((((int) stateByte) + STEP) % 256);
        return stateByte;
    }
    
}
