package hu.webarticum.holodb.testing;

public final class DebugUtil {

    private DebugUtil() {
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
