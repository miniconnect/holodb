package hu.webarticum.holodb.core.data.hasher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class FastHasherTest {

    @Test
    void testIllegalConstructorParameters() {
        assertThatThrownBy(() -> new FastHasher(new byte[1], -5)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new FastHasher(new byte[1], 0)).isInstanceOf(IllegalArgumentException.class);
        new FastHasher(new byte[0], 10);
    }

    @Test
    void testHashSize() {
        assertThat(new FastHasher(b("2468"), 8).hash(b("275837423"))).hasSize(8);
        assertThat(new FastHasher(b("987654"), 19).hash(b("859328438594352"))).hasSize(19);
        assertThat(new FastHasher(b("1234"), 257).hash(b("76543"))).hasSize(257);
    }


    private byte[] b(String content) {
        return content.getBytes(StandardCharsets.UTF_8);
    }

}
