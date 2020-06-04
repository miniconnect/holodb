package hu.webarticum.holodb.data.hasher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class FastHasherTest {

    @Test
    void testIllegalConstructorParameters() {
        assertThatThrownBy(() -> new FastHasher(-5)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new FastHasher(0)).isInstanceOf(IllegalArgumentException.class);
        new FastHasher(new byte[0], 10);
    }

    @Test
    void testHashSize() {
        assertThat(new FastHasher(7).hash(b("1234"))).hasSize(7);
        assertThat(new FastHasher(16).hash(b("463723"))).hasSize(16);
        assertThat(new FastHasher(b("2468"), 8).hash(b("275837423"))).hasSize(8);
        assertThat(new FastHasher(b("987654"), 19).hash(b("859328438594352"))).hasSize(19);
    }

    
    private byte[] b(String content) {
        return content.getBytes(StandardCharsets.UTF_8);
    }
    
}
