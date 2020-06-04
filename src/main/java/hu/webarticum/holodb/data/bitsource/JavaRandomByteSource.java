package hu.webarticum.holodb.data.bitsource;

import java.util.Random;

public class JavaRandomByteSource implements ByteSource {

    private final Random random;
    
    
    public JavaRandomByteSource() {
        this(0);
    }

    public JavaRandomByteSource(long seed) {
        this.random = new Random(seed);
    }
    
    
    @Override
    public byte next() {
        byte[] bytes = new byte[1];
        random.nextBytes(bytes);
        return bytes[0];
    }

}
