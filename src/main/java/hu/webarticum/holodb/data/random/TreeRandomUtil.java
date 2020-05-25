package hu.webarticum.holodb.data.random;

import java.nio.ByteBuffer;

public final class TreeRandomUtil {

    private TreeRandomUtil() {
        // utility
    }
    

    public static boolean fetchBoolean(TreeRandom treeRandom) {
        byte[] bytes = treeRandom.getBytes(1);
        return ((bytes[0] & 1) > 0);
    }

    public static byte fetchByte(TreeRandom treeRandom) {
        byte[] bytes = treeRandom.getBytes(1);
        return bytes[0];
    }

    public static char fetchChar(TreeRandom treeRandom) {
        byte[] bytes = treeRandom.getBytes(2);
        return ByteBuffer.wrap(bytes).getChar();
    }

    public static short fetchShort(TreeRandom treeRandom) {
        byte[] bytes = treeRandom.getBytes(2);
        return ByteBuffer.wrap(bytes).getShort();
    }

    public static int fetchInt(TreeRandom treeRandom) {
        byte[] bytes = treeRandom.getBytes(4);
        return ByteBuffer.wrap(bytes).getInt();
    }

    public static long fetchLong(TreeRandom treeRandom) {
        byte[] bytes = treeRandom.getBytes(8);
        return ByteBuffer.wrap(bytes).getLong();
    }

    public static float fetchFloat(TreeRandom treeRandom) {
        byte[] bytes = treeRandom.getBytes(4);
        return ByteBuffer.wrap(bytes).getFloat();
    }

    public static double fetchDouble(TreeRandom treeRandom) {
        byte[] bytes = treeRandom.getBytes(8);
        return ByteBuffer.wrap(bytes).getDouble();
    }
    
}
