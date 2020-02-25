package hu.webarticum.holodb.util;

import java.math.BigInteger;
import java.util.function.Supplier;

public class BitSource {

    private byte[] bytes;
    
    private final Supplier<Byte> byteSupplier;
    
    private int position = 0;
    
    
    public BitSource(byte[] initialBuffer, Supplier<Byte> byteSupplier) {
        this.bytes = new byte[initialBuffer.length];
        System.arraycopy(initialBuffer, 0, this.bytes, 0, initialBuffer.length);
        this.byteSupplier = byteSupplier;
    }
    
    
    public BigInteger next(int length) {
        int newPosition = position + length;
        ensureBitLength(newPosition);
        
        // TODO
        return BigInteger.ZERO;
        
    }
    
    private void ensureBitLength(int length) {
        if (length > bytes.length * 8) {
            int remaining = length % 8;
            int newByteLength = length / 8;
            if (remaining > 0) {
                newByteLength++;
            }
            int oldByteLength = bytes.length;
            byte[] newBytes = new byte[newByteLength];
            System.arraycopy(bytes, 0, newBytes, 0, oldByteLength);
            for (int b = oldByteLength; b < newByteLength; b++) {
                newBytes[b] = byteSupplier.get();
            }
            bytes = newBytes;
        }
    }
    
}
