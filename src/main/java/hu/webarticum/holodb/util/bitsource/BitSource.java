package hu.webarticum.holodb.util.bitsource;

public class BitSource {
    
    private byte[] bytes;
    
    private final ByteSource byteSource;
    
    private int position = 0;
    

    public BitSource(ByteSource byteSource) {
        this.bytes = new byte[0];
        this.byteSource = byteSource;
    }

    public BitSource(byte[] initialBuffer) {
        this(initialBuffer, new ZeroByteSource());
    }
    
    public BitSource(byte[] initialBuffer, ByteSource byteSource) {
        this.bytes = new byte[initialBuffer.length];
        System.arraycopy(initialBuffer, 0, this.bytes, 0, initialBuffer.length);
        this.byteSource = byteSource;
    }
    
    
    public byte[] fetch(int numberOfBits) {
        System.out.println("BEFORE ENSURE...");
        dump();
        System.out.println();
        
        ensureBytes(numberOfBits);

        System.out.println("AFTER ENSURE...");
        dump();
        System.out.println();
        
        int leadingLength = numberOfBits % 8;
        int numberOfWholeBytes = numberOfBits / 8;
        int numberOfBytes = leadingLength == 0 ? numberOfWholeBytes : numberOfWholeBytes + 1;
        byte[] result = new byte[numberOfBytes];
        
        int resultByteIndex = 0;
        int readPosition = position;
        if (leadingLength > 0) {
            result[resultByteIndex] = extractPadded(readPosition, leadingLength);
            resultByteIndex++;
            readPosition += leadingLength;
        }
        
        for ( ; resultByteIndex < numberOfBytes; resultByteIndex++) {
            result[resultByteIndex] = extract(readPosition, 8);
            readPosition += 8;
        }
        
        position = readPosition;

        return result;
        
    }
    
    private void ensureBytes(int numberOfBits) {
        if (position + numberOfBits <= bytes.length * 8) {
            return;
        }
        
        int keepingBytesIndex = position / 8;
        int keepingBytesCount = bytes.length - keepingBytesIndex;
        int neededBitsCount = (position % 8) + numberOfBits;
        int neededBytesCount = neededBitsCount % 8 == 0 ? neededBitsCount / 8 : (neededBitsCount / 8) + 1;
        
        byte[] newBytes = new byte[neededBytesCount];
        System.arraycopy(bytes, keepingBytesIndex, newBytes, 0, keepingBytesCount);

        for (int b = keepingBytesCount; b < neededBytesCount; b++) {
            newBytes[b] = byteSource.next();
        }

        bytes = newBytes;
        position %= 8;
    }

    private byte extractPadded(int from, int length) {
        int byteIndex = from / 8;
        int shift = from % 8;
        
        if (shift + length <= 8) {
            return (byte) (((bytes[byteIndex] << shift) & 0xFF) >>> (8 - length));
        }

        byte left = (byte) (((bytes[byteIndex] << shift) & 0xFF) >>> (8 - length));
        byte right = (byte) ((bytes[byteIndex + 1] & 0xFF) >>> (16 - shift - length));
        return (byte) (left | right);
    }
    
    private byte extract(int from, int length) {
        int byteIndex = from / 8;
        int shift = from % 8;
        
        if (shift == 0) {
            return bytes[byteIndex];
        }
        
        byte left = (byte) (bytes[byteIndex] << shift);
        byte right = (byte) ((bytes[byteIndex + 1] & 0xFF) >>> (8 - shift));
        return (byte) (left | right);
    }
    
    
    
    
    
    
    
    
    
    // XXX
    public void dump() {
        System.out.println(byteArrayToString(bytes));
        int before = position + (position / 8) + 1;
        for (int i = 0; i < before; i++) {
            System.out.print(" ");
        }
        System.out.println("`- (" + position + ")");
    }

    public static String byteArrayToString(byte[] bytes) {
        StringBuilder resultBuilder = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            resultBuilder.append('|');
            resultBuilder.append(byteToString(bytes[i]));
        }
        return resultBuilder.toString();
    }
    
    public static String byteToString(byte b) {
        String unpadded = Integer.toString(Byte.toUnsignedInt(b), 2);
        
        int remainingLength = 8 - unpadded.length();
        if (remainingLength == 0) {
            return unpadded;
        }
        
        StringBuilder resultBuilder = new StringBuilder();
        for (int i = 0; i < remainingLength; i++) {
            resultBuilder.append('0');
        }
        resultBuilder.append(unpadded);
        
        return resultBuilder.toString();
    }
    
}
