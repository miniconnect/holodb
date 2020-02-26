package hu.webarticum.holodb.util.bitsource;

import java.util.Random;

public class JavaRandomByteSource implements ByteSource {

    private final Random random;
    
    
    public JavaRandomByteSource() {
        this(new Random());
    }
    
    public JavaRandomByteSource(Random random) {
        this.random = random;
    }
    
    
    @Override
    public byte next() {
        byte[] bytes = new byte[1];
        random.nextBytes(bytes);
        return bytes[0];
    }

}
