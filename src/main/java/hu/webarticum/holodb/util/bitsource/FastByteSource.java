package hu.webarticum.holodb.util.bitsource;

public class FastByteSource implements ByteSource {

    private static final int MULITPLIER = 1234567;

    private static final int COUNTER_START = 63;

    
    private int counter = COUNTER_START;
    
    private byte stateByte;
    

    public FastByteSource() {
        this((byte) 0);
    }

    public FastByteSource(byte seed) {
        this.stateByte = seed;
    }
    

    @Override
    public byte next() {
        stateByte = (byte) ((((int) stateByte) * MULITPLIER) + counter);
        counter++;
        return stateByte;
    }
    
}
