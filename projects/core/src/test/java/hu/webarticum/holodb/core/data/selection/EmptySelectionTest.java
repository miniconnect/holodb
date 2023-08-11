package hu.webarticum.holodb.core.data.selection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import hu.webarticum.miniconnect.lang.LargeInteger;

class EmptySelectionTest {

    @Test
    void test() {
        EmptySelection emptySelection = EmptySelection.instance();
        assertThat(emptySelection.isEmpty()).isTrue();
        assertThat(emptySelection.size()).isEqualTo(LargeInteger.ZERO);
        assertThat(emptySelection.iterator().hasNext()).isFalse();
        assertThatThrownBy(() -> emptySelection.at(LargeInteger.ZERO)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThat(emptySelection.contains(LargeInteger.ZERO)).isFalse();
        assertThat(emptySelection.reverseOrder()).isEmpty();
    }

}
