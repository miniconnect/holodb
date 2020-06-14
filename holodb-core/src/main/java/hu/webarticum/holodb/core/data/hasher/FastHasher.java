package hu.webarticum.holodb.core.data.hasher;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import hu.webarticum.holodb.core.util.ByteUtil;

public class FastHasher implements Hasher {

    private static final int DEFAULT_HASH_LENGTH = 8;
    
    
    private static Hasher keyHasher;
    

    private final byte[] key;
    

    public FastHasher() {
        this(0L);
    }

    public FastHasher(long key) {
        this(ByteUtil.longToBytes(key));
    }

    public FastHasher(BigInteger key) {
        this(key.toByteArray());
    }

    public FastHasher(String key) {
        this(key.getBytes(StandardCharsets.UTF_8));
    }
    
    public FastHasher(byte[] key) {
        this(key, DEFAULT_HASH_LENGTH);
    }
    
    public FastHasher(long key, int hashLength) {
        this(ByteUtil.longToBytes(key), hashLength);
    }

    public FastHasher(BigInteger key, int hashLength) {
        this(key.toByteArray(), hashLength);
    }

    public FastHasher(String key, int hashLength) {
        this(key.getBytes(StandardCharsets.UTF_8), hashLength);
    }
    
    public FastHasher(byte[] key, int hashLength) {
        if (hashLength < 1) {
            throw new IllegalArgumentException(String.format("Hash length must be positive, %d given", hashLength));
        }
        
        this.key = new byte[hashLength];
        ByteUtil.fillBytesFrom(this.key, getKeyHasher().hash(key));
    }

    private static Hasher getKeyHasher() {
        if (keyHasher == null) {
            keyHasher = new Sha256MacHasher();
        }
        return keyHasher;
    }
    

    @Override
    public byte[] hash(byte[] input) {
        int keyLength = key.length;
        int inputLength = input.length;
        
        byte[] result = Arrays.copyOf(key, keyLength);

        int state = 1;
        int inputCounter = 0;
        
        int fullCycles = inputLength / keyLength;
        for (int i = 0; i < fullCycles; i++) {
            for (int j = 0; j < keyLength; j++, inputCounter++) {
                state = (state * 31) + (result[j] ^ input[inputCounter]);
                result[j] = (byte) state;
            }
        }
        
        int inputRemainingLength = inputLength - (fullCycles * keyLength);
        if (inputRemainingLength > 0) {
            for (int i = 0; i < inputRemainingLength; i++, inputCounter++) {
                state = (state * 31) + (result[i] ^ input[inputCounter]);
                result[i] = (byte) state;
            }
            
            for (int i = inputRemainingLength; i < keyLength; i++) {
                state = (state * 31) + result[i];
                result[i] = (byte) state;
            }
        }
        
        return result;
    }
    
}
