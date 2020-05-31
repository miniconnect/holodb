package hu.webarticum.holodb.util.bitsource;

// TODO: review, more research, optimize, ensure all values
//  (although this seems relatively good with these magic numbers)
public class FastByteSource implements ByteSource {

    private static final int MULITPLIER = 1234567;

    private static final int RESET_MIN_COUNTER = 5;
    
    private static final byte RESET_MIN_STATE_BYTE = (byte) 100;
    
    private int counter = 0;
    
    private byte stateByte;
    

    public FastByteSource() {
        this((byte) 0);
    }

    public FastByteSource(byte seed) {
        this.stateByte = seed;
    }
    

    @Override
    public byte next() {
        stateByte = (byte) ((((int) stateByte) * MULITPLIER) + 1);
        counter++;
        if (counter > RESET_MIN_COUNTER && stateByte > RESET_MIN_STATE_BYTE) {
            counter = 0;
            stateByte--;
        }
        return stateByte;
    }
    
}
