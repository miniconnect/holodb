package hu.webarticum.holodb.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.BitSet;

import org.junit.jupiter.api.Test;

public class ByteUtilTest {

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
    
}
