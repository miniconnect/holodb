package hu.webarticum.holodb.util.bitsource;

public class FastByteSource implements ByteSource {

    private static final int STEP = 17 * 31 * 43;
    
    
    private byte stateByte;
    

    public FastByteSource() {
        this((byte) 0);
    }

    public FastByteSource(byte seed) {
        this.stateByte = seed;
    }
    
    
    @Override
    public byte next() {
        stateByte = (byte) (((int) stateByte) + STEP);
        return stateByte;
    }
    
}
