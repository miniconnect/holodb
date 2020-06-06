package hu.webarticum.holodb.util;

import java.nio.ByteBuffer;
import java.util.BitSet;

public final class ByteUtil {

    private ByteUtil() {
    }

    
    public static byte[] intToBytes(int number) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(number);
        return buffer.array();
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
    
    public static String bytesToBinaryString(byte[] bytes) {
        StringBuilder resultBuilder = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            if (i > 0) {
                resultBuilder.append(' ');
            }
            resultBuilder.append(byteToBinaryString(bytes[i]));
        }
        return resultBuilder.toString();
    }
    
    public static String byteToBinaryString(byte b) {
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

    public static String bytesToHexadecimalString(byte[] bytes) {
        StringBuilder resultBuilder = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            if (i > 0) {
                resultBuilder.append(' ');
            }
            resultBuilder.append(byteToHexadecimalString(bytes[i]));
        }
        return resultBuilder.toString();
    }

    public static String byteToHexadecimalString(byte b) {
        String leftDigit = Integer.toHexString(Byte.toUnsignedInt(b) / 16);
        String rightDigit = Integer.toHexString(Byte.toUnsignedInt(b) % 16);
        return leftDigit + rightDigit;
    }

    public static int getClosestSetBit(BitSet bitSet, int index) {
        if (bitSet.get(index)) {
            return index;
        }

        int nextIndex = bitSet.nextSetBit(index);
        int previousIndex = bitSet.previousSetBit(index);
        
        if (nextIndex == (-1)) {
            return previousIndex;
        } else if (previousIndex == (-1)) {
            return nextIndex;
        } else {
            return (index - previousIndex > nextIndex - index) ? nextIndex : previousIndex;
        }
    }
    
    public static void fillBytesFrom(byte[] target, byte[] source) {
        int targetLength = target.length;
        int sourceLength = source.length;
        
        if (sourceLength == 0 || targetLength == 0) {
            return;
        }
        
        int commonLength = targetLength < sourceLength ? targetLength : sourceLength;
        System.arraycopy(source, 0, target, 0, commonLength);
        
        if (targetLength > sourceLength) {
            int times = targetLength / sourceLength;
            for (int i = 1; i < times; i++) {
                int start = i * sourceLength;
                System.arraycopy(source, 0, target, start, sourceLength);
            }
            int end = sourceLength * times;
            int tailLength = targetLength - end;
            if (tailLength > 0) {
                System.arraycopy(source, 0, target, end, tailLength);
            }
        }
    }
    
}
