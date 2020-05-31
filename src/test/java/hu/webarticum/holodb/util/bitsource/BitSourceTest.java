package hu.webarticum.holodb.util.bitsource;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BitSourceTest {

    @Test
    void testEmpty1() {
        ByteSourceBitSource bitSource = createEmpty();
        assertThat(bitSource.fetch(3)).isEqualTo(new byte[] { 0 });
        assertThat(bitSource.fetch(8)).isEqualTo(new byte[] { 0 });
        assertThat(bitSource.fetch(11)).isEqualTo(new byte[] { 0, 0 });
        assertThat(bitSource.fetch(17)).isEqualTo(new byte[] { 0, 0, 0 });
    }

    @Test
    void testEmpty2() {
        ByteSourceBitSource bitSource = createEmpty();
        assertThat(bitSource.fetch(8)).isEqualTo(new byte[] { 0 });
        assertThat(bitSource.fetch(8)).isEqualTo(new byte[] { 0 });
        assertThat(bitSource.fetch(16)).isEqualTo(new byte[] { 0, 0 });
        assertThat(bitSource.fetch(1)).isEqualTo(new byte[] { 0 });
        assertThat(bitSource.fetch(33)).isEqualTo(new byte[] { 0, 0, 0, 0, 0 });
    }
    
    @Test
    void testWithInitialBuffer1() {
        ByteSourceBitSource bitSource = createWithInitialBuffer();
        assertThat(bitSource.fetch(3)).isEqualTo(new byte[] { 0b010 });
        assertThat(bitSource.fetch(7)).isEqualTo(new byte[] { 0b1010111 });
        assertThat(bitSource.fetch(10)).isEqualTo(new byte[] { 0b11, (byte) 0b11110000 });
        assertThat(bitSource.fetch(2)).isEqualTo(new byte[] { 0 });
    }

    @Test
    void testWithInitialBuffer2() {
        ByteSourceBitSource bitSource = createWithInitialBuffer();
        assertThat(bitSource.fetch(2)).isEqualTo(new byte[] { 0b01 });
        assertThat(bitSource.fetch(2)).isEqualTo(new byte[] { 0b01 });
        assertThat(bitSource.fetch(18)).isEqualTo(new byte[] { 0b01, 0b01111111, (byte) 0b11000000 });
        assertThat(bitSource.fetch(12)).isEqualTo(new byte[] { 0, 0 });
    }

    @Test
    void testWithByteSource1() {
        ByteSourceBitSource bitSource = createWithByteSource();
        assertThat(bitSource.fetch(3)).isEqualTo(new byte[] { 0b000 });
        assertThat(bitSource.fetch(4)).isEqualTo(new byte[] { 0b0101 });
        assertThat(bitSource.fetch(5)).isEqualTo(new byte[] { 0b00001 });
        assertThat(bitSource.fetch(12)).isEqualTo(new byte[] { 0b0100, 0b00011110 });
    }

    @Test
    void testWithByteSource2() {
        ByteSourceBitSource bitSource = createWithByteSource();
        assertThat(bitSource.fetch(9)).isEqualTo(new byte[] { 0, 0b00010100 });
        assertThat(bitSource.fetch(19)).isEqualTo(new byte[] { 0b001, 0b01000001, (byte) 0b11100010 });
        assertThat(bitSource.fetch(2)).isEqualTo(new byte[] { 0b10 });
    }

    @Test
    void testWithInitialBufferAndByteSource1() {
        ByteSourceBitSource bitSource = createWithInitialBufferAndByteSource();
        assertThat(bitSource.fetch(4)).isEqualTo(new byte[] { 0b0101 });
        assertThat(bitSource.fetch(9)).isEqualTo(new byte[] { 0, (byte) 0b10111111 });
        assertThat(bitSource.fetch(6)).isEqualTo(new byte[] { 0b111000 });
        assertThat(bitSource.fetch(15)).isEqualTo(new byte[] { 0b0101000, 0b01010000 });
    }

    @Test
    void testWithInitialBufferAndByteSource2() {
        ByteSourceBitSource bitSource = createWithInitialBufferAndByteSource();
        assertThat(bitSource.fetch(3)).isEqualTo(new byte[] { 0b010 });
        assertThat(bitSource.fetch(13)).isEqualTo(new byte[] { 0b10101, (byte) 0b11111111 });
        assertThat(bitSource.fetch(5)).isEqualTo(new byte[] { 0b00001 });
        assertThat(bitSource.fetch(10)).isEqualTo(new byte[] { 0b01,  0b00001010 });
    }

    @Test
    void testWithInitialBufferAndByteSource3() {
        ByteSourceBitSource bitSource = createWithInitialBufferAndByteSource();
        assertThat(bitSource.fetch(21)).isEqualTo(new byte[] { 0b01010, (byte) 0b10111111, (byte) 0b11100001 });
        assertThat(bitSource.fetch(16)).isEqualTo(new byte[] { 0b01000010, (byte) 0b10000011 });
        assertThat(bitSource.fetch(15)).isEqualTo(new byte[] { 0b1100010, (byte) 0b10000011 });
        assertThat(bitSource.fetch(7)).isEqualTo(new byte[] { 0b0010001 });
        assertThat(bitSource.fetch(11)).isEqualTo(new byte[] { 0b111, 0b00010001 });
    }

    @Test
    void testWithInitialBufferAndByteSource4() {
        ByteSourceBitSource bitSource = createWithInitialBufferAndByteSource();
        assertThat(bitSource.fetch(5)).isEqualTo(new byte[] { 0b01010 });
        assertThat(bitSource.fetch(66)).isEqualTo(new byte[] {
                0b10, (byte) 0b11111111, (byte) 0b10000101, 0b00001010,
                0b00001111, 0b00010100, 0b00011001, 0b00011110, 0b00100011 });
    }
    
    private ByteSourceBitSource createEmpty() {
        return new ByteSourceBitSource(new byte[0]);
    }

    private ByteSourceBitSource createWithInitialBuffer() {
        return new ByteSourceBitSource(createBuffer());
    }

    private ByteSourceBitSource createWithByteSource() {
        return new ByteSourceBitSource(createByteSource());
    }
    
    private ByteSourceBitSource createWithInitialBufferAndByteSource() {
        return new ByteSourceBitSource(createBuffer(), createByteSource());
    }

    private byte[] createBuffer() {
        return new byte[] { 0b01010101, (byte) 0b11111111 };
    }
    
    private ByteSource createByteSource() {
        //       10       20       30       40       50       60       70
        // 00001010 00010100 00011110 00101000 00110010 00111100 01000110 ...
        int[] i = new int[] { 0 };
        return () -> (byte) (++i[0] * 10);
    }
    
}
