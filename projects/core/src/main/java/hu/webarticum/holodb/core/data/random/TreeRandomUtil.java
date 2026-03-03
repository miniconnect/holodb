package hu.webarticum.holodb.core.data.random;

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

    // FIXME: bias?
    public static float fetchSmallFloat(TreeRandom treeRandom) {
        int intValue = fetchInt(treeRandom);
        if (intValue == Integer.MIN_VALUE) {
            return 0f;
        }
        return Math.abs(intValue) / (float) Integer.MAX_VALUE;
    }

    public static double fetchDouble(TreeRandom treeRandom) {
        byte[] bytes = treeRandom.getBytes(8);
        return ByteBuffer.wrap(bytes).getDouble();
    }

    // FIXME: bias?
    public static double fetchSmallDouble(TreeRandom treeRandom) {
        long longValue = fetchLong(treeRandom);
        if (longValue == Long.MIN_VALUE) {
            return 0L;
        }
        return Math.abs(longValue) / (double) Long.MAX_VALUE;
    }

}
