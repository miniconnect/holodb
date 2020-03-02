package hu.webarticum.holodb.util;

import java.nio.ByteBuffer;

public final class ByteUtil {

    private ByteUtil() {
    }
    
    public static byte[] longToBytes(long number) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(number);
        return buffer.array();
    }

    public static long firstBytesToLong(byte[] bytes) {
        byte[] adjustedBytes;
        if (bytes.length == Long.BYTES) {
            adjustedBytes = bytes;
        } else {
            adjustedBytes = new byte[Long.BYTES];
            int copyLength = bytes.length >= Long.BYTES ? Long.BYTES : bytes.length;
            System.arraycopy(bytes, 0, adjustedBytes, 0, copyLength);
        }
        return bytesToLong(adjustedBytes);
    }

    public static long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes);
        buffer.flip();
        return buffer.getLong();
    }
    
    public static String bytesToString(byte[] bytes) {
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