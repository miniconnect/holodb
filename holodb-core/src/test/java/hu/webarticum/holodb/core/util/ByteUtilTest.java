package hu.webarticum.holodb.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.BitSet;

import org.junit.jupiter.api.Test;

import hu.webarticum.holodb.core.util.ByteUtil;
import hu.webarticum.holodb.core.util.ObjectUtil;

public class ByteUtilTest {

    @Test
    void testIntToBytes() {
        assertThat(0).extracting(ByteUtil::intToBytes).isEqualTo(b(0, 0, 0, 0));
        assertThat(1).extracting(ByteUtil::intToBytes).isEqualTo(b(0, 0, 0, 1));
        assertThat(43).extracting(ByteUtil::intToBytes).isEqualTo(b(0, 0, 0, 43));
        assertThat(256).extracting(ByteUtil::intToBytes).isEqualTo(b(0, 0, 1, 0));
        assertThat(13191175).extracting(ByteUtil::intToBytes).isEqualTo(b(0, 201, 72, 7));
        assertThat(-1).extracting(ByteUtil::intToBytes).isEqualTo(b(255, 255, 255, 255));
        assertThat(-78238062).extracting(ByteUtil::intToBytes).isEqualTo(b(251, 86, 46, 146));
    }

    @Test
    void testLongToBytes() {
        assertThat(0L).extracting(ByteUtil::longToBytes).isEqualTo(b(0, 0, 0, 0, 0, 0, 0, 0));
        assertThat(1L).extracting(ByteUtil::longToBytes).isEqualTo(b(0, 0, 0, 0, 0, 0, 0, 1));
        assertThat(117L).extracting(ByteUtil::longToBytes).isEqualTo(b(0, 0, 0, 0, 0, 0, 0, 117));
        assertThat(256L).extracting(ByteUtil::longToBytes).isEqualTo(b(0, 0, 0, 0, 0, 0, 1, 0));
        assertThat(13191175L).extracting(ByteUtil::longToBytes).isEqualTo(b(0, 0, 0, 0, 0, 201, 72, 7));
        assertThat(878981819498590340L).extracting(ByteUtil::longToBytes).isEqualTo(b(12, 50, 197, 78, 203, 61, 112, 132));
        assertThat(-1L).extracting(ByteUtil::longToBytes).isEqualTo(b(255, 255, 255, 255, 255, 255, 255, 255));
        assertThat(-78238062L).extracting(ByteUtil::longToBytes).isEqualTo(b(255, 255, 255, 255, 251, 86, 46, 146));
        assertThat(-2653304120892283044L).extracting(ByteUtil::longToBytes).isEqualTo(b(219, 45, 145, 245, 185, 34, 167, 92));
    }
    
    @Test
    void testFirstBytesToLong() {
        assertThat(ByteUtil.firstBytesToLong(b(0, 0, 0, 0, 0, 1))).isEqualTo(65536L);
        assertThat(ByteUtil.firstBytesToLong(b(0, 0, 0, 0, 0, 201, 72, 7, 45, 31, 122, 255, 1, 0, 1))).isEqualTo(13191175L);
        assertThat(ByteUtil.firstBytesToLong(b(12, 50, 197, 78, 203, 61, 112, 132, 32, 45, 174, 63))).isEqualTo(878981819498590340L);
        assertThat(ByteUtil.firstBytesToLong(b(219, 45, 145, 245, 185, 34, 167, 92, 99, 192, 3, 204))).isEqualTo(-2653304120892283044L);
    }

    @Test
    void testBytesToLong() {
        assertThat(ByteUtil.bytesToLong(b(0, 0, 0, 0, 0, 201, 72, 7))).isEqualTo(13191175L);
        assertThat(ByteUtil.bytesToLong(b(12, 50, 197, 78, 203, 61, 112, 132))).isEqualTo(878981819498590340L);
        assertThat(ByteUtil.bytesToLong(b(219, 45, 145, 245, 185, 34, 167, 92))).isEqualTo(-2653304120892283044L);
    }
    
    @Test
    void testBytesToBinaryString() {
        assertThat(ByteUtil.bytesToBinaryString(b())).isEqualTo("");
        assertThat(ByteUtil.bytesToBinaryString(b(15))).isEqualTo("00001111");
        assertThat(ByteUtil.bytesToBinaryString(b(43, 125))).isEqualTo("00101011 01111101");
        assertThat(ByteUtil.bytesToBinaryString("apple".getBytes(StandardCharsets.UTF_8))).isEqualTo("01100001 01110000 01110000 01101100 01100101");
    }

    @Test
    void testByteToBinaryString() {
        assertThat(ByteUtil.byteToBinaryString((byte) 0)).isEqualTo("00000000");
        assertThat(ByteUtil.byteToBinaryString((byte) 17)).isEqualTo("00010001");
        assertThat(ByteUtil.byteToBinaryString((byte) 125)).isEqualTo("01111101");
        assertThat(ByteUtil.byteToBinaryString((byte) 255)).isEqualTo("11111111");
    }

    @Test
    void testBytesToHexadecimalString() {
        assertThat(ByteUtil.bytesToHexadecimalString(b())).isEqualToIgnoringCase("");
        assertThat(ByteUtil.bytesToHexadecimalString(b(15))).isEqualToIgnoringCase("0F");
        assertThat(ByteUtil.bytesToHexadecimalString(b(43, 125))).isEqualToIgnoringCase("2B 7D");
        assertThat(ByteUtil.bytesToHexadecimalString("apple".getBytes(StandardCharsets.UTF_8))).isEqualToIgnoringCase("61 70 70 6C 65");
    }

    @Test
    void testByteToHexadecimalString() {
        assertThat(ByteUtil.byteToHexadecimalString((byte) 0)).isEqualToIgnoringCase("00");
        assertThat(ByteUtil.byteToHexadecimalString((byte) 17)).isEqualToIgnoringCase("11");
        assertThat(ByteUtil.byteToHexadecimalString((byte) 125)).isEqualToIgnoringCase("7D");
        assertThat(ByteUtil.byteToHexadecimalString((byte) 255)).isEqualToIgnoringCase("FF");
    }
    
    @Test
    void testGetClosestSetBit() {
        assertThat(ByteUtil.getClosestSetBit(buildBitSet(), 0)).isEqualTo(-1);
        assertThat(ByteUtil.getClosestSetBit(buildBitSet(), 2)).isEqualTo(-1);
        assertThat(ByteUtil.getClosestSetBit(buildBitSet(), 115)).isEqualTo(-1);
        assertThat(ByteUtil.getClosestSetBit(buildBitSet(2, 4, 5), 4)).isEqualTo(4);
        assertThat(ByteUtil.getClosestSetBit(buildBitSet(2, 4, 5), 5)).isEqualTo(5);
        assertThat(ByteUtil.getClosestSetBit(buildBitSet(2, 4, 5), 7)).isEqualTo(5);
        assertThat(ByteUtil.getClosestSetBit(buildBitSet(2, 4, 5), 2)).isEqualTo(2);
        assertThat(ByteUtil.getClosestSetBit(buildBitSet(2, 4, 5), 1)).isEqualTo(2);
        assertThat(ByteUtil.getClosestSetBit(buildBitSet(2, 4, 5), 0)).isEqualTo(2);
        assertThat(ByteUtil.getClosestSetBit(buildBitSet(2, 4, 5), 3)).isEqualTo(2);
        assertThat(ByteUtil.getClosestSetBit(buildBitSet(2, 6, 8), 4)).isEqualTo(2);
        assertThat(ByteUtil.getClosestSetBit(buildBitSet(2, 6, 8), 3)).isEqualTo(2);
        assertThat(ByteUtil.getClosestSetBit(buildBitSet(2, 6, 8), 5)).isEqualTo(6);
        assertThat(ByteUtil.getClosestSetBit(buildBitSet(72, 235), 14)).isEqualTo(72);
        assertThat(ByteUtil.getClosestSetBit(buildBitSet(72, 235), 72)).isEqualTo(72);
        assertThat(ByteUtil.getClosestSetBit(buildBitSet(72, 235), 117)).isEqualTo(72);
        assertThat(ByteUtil.getClosestSetBit(buildBitSet(72, 235), 195)).isEqualTo(235);
        assertThat(ByteUtil.getClosestSetBit(buildBitSet(72, 235), 235)).isEqualTo(235);
        assertThat(ByteUtil.getClosestSetBit(buildBitSet(72, 235), 657)).isEqualTo(235);
    }

    @Test
    void testFillBytesFrom() {
        assertThat(ObjectUtil.apply(b(""), bytes -> ByteUtil.fillBytesFrom(bytes, b("")))).isEqualTo(b(""));
        assertThat(ObjectUtil.apply(b(""), bytes -> ByteUtil.fillBytesFrom(bytes, b("abc")))).isEqualTo(b(""));
        assertThat(ObjectUtil.apply(b("0123"), bytes -> ByteUtil.fillBytesFrom(bytes, b("")))).isEqualTo(b("0123"));
        assertThat(ObjectUtil.apply(b("0123"), bytes -> ByteUtil.fillBytesFrom(bytes, b("a")))).isEqualTo(b("aaaa"));
        assertThat(ObjectUtil.apply(b("0123"), bytes -> ByteUtil.fillBytesFrom(bytes, b("ab")))).isEqualTo(b("abab"));
        assertThat(ObjectUtil.apply(b("0123"), bytes -> ByteUtil.fillBytesFrom(bytes, b("abc")))).isEqualTo(b("abca"));
        assertThat(ObjectUtil.apply(b("0123"), bytes -> ByteUtil.fillBytesFrom(bytes, b("abcd")))).isEqualTo(b("abcd"));
        assertThat(ObjectUtil.apply(b("0123"), bytes -> ByteUtil.fillBytesFrom(bytes, b("abcde")))).isEqualTo(b("abcd"));
        assertThat(ObjectUtil.apply(b("0123"), bytes -> ByteUtil.fillBytesFrom(bytes, b("abcdef")))).isEqualTo(b("abcd"));
    }

    
    private BitSet buildBitSet(int... sortedValues) {
        int length = sortedValues.length > 1 ? sortedValues[sortedValues.length - 1] + 1 : 1;
        BitSet bitSet = new BitSet(length);
        for (int value : sortedValues) {
            bitSet.set(value);
        }
        return bitSet;
    }
    
    private byte[] b(String content) {
        return content.getBytes(StandardCharsets.UTF_8);
    }

    private byte[] b(int... byteNumbers) {
        byte[] result = new byte[byteNumbers.length];
        for (int i = 0; i < byteNumbers.length; i++) {
            result[i] = (byte) byteNumbers[i];
        }
        return result;
    }
    
}
