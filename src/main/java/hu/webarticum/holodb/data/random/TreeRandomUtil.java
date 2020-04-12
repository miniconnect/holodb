package hu.webarticum.holodb.data.random;

import java.nio.ByteBuffer;

public final class TreeRandomUtil {

    private TreeRandomUtil() {
        // utility
    }
    

    public static boolean getBoolean(TreeRandom treeRandom) {
        byte[] bytes = treeRandom.getBytes(1);
        return ((bytes[0] & 1) > 0);
    }

    public static byte getByte(TreeRandom treeRandom) {
        byte[] bytes = treeRandom.getBytes(1);
        return bytes[0];
    }

    public static char getChar(TreeRandom treeRandom) {
        byte[] bytes = treeRandom.getBytes(2);
        return ByteBuffer.wrap(bytes).getChar();
    }

    public static short getShort(TreeRandom treeRandom) {
        byte[] bytes = treeRandom.getBytes(2);
        return ByteBuffer.wrap(bytes).getShort();
    }

    public static int getInt(TreeRandom treeRandom) {
        byte[] bytes = treeRandom.getBytes(4);
        return ByteBuffer.wrap(bytes).getInt();
    }

    public static long getLong(TreeRandom treeRandom) {
        byte[] bytes = treeRandom.getBytes(8);
        return ByteBuffer.wrap(bytes).getLong();
    }

    public static float getFloat(TreeRandom treeRandom) {
        byte[] bytes = treeRandom.getBytes(4);
        return ByteBuffer.wrap(bytes).getFloat();
    }

    public static double getDouble(TreeRandom treeRandom) {
        byte[] bytes = treeRandom.getBytes(8);
        return ByteBuffer.wrap(bytes).getDouble();
    }
    
}
