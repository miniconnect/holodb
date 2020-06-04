package hu.webarticum.holodb.data.hasher;

import java.util.Arrays;

import hu.webarticum.holodb.util.ByteUtil;

public class FastHasher implements Hasher {

    private static Hasher keyHasher;
    

    private final byte[] key;
    

    public FastHasher() {
        this(8);
    }
    
    public FastHasher(int hashLength) {
        this(new byte[0], hashLength);
    }
    
    public FastHasher(byte[] key, int hashLength) {
        if (hashLength < 1) {
            throw new IllegalArgumentException("Hash length must be positive");
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
