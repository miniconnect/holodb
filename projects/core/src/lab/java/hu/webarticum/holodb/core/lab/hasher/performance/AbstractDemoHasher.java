package hu.webarticum.holodb.core.lab.hasher.performance;

import java.util.Arrays;

import hu.webarticum.holodb.core.data.hasher.Hasher;

public abstract class AbstractDemoHasher implements Hasher {

    private final byte[] key;
    
    
    protected AbstractDemoHasher(byte[] key) {
        this.key = key;
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
