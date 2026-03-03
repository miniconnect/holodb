package hu.webarticum.holodb.regex.charclass;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

class SimpleEntryListTest {

    @Test
    void testEmpty() {
        SimpleEntryList<Integer, String> entries = new SimpleEntryList<>();
        assertThat(entries).isEmpty();
        assertThat(entries.isEmpty()).isTrue();
        assertThat(entries.size()).isZero();
        assertThat(entries.iterator()).isExhausted();
        assertThatThrownBy(entries::last).isInstanceOf(NoSuchElementException.class);
        assertThatThrownBy(entries::removeLast).isInstanceOf(NoSuchElementException.class);
        assertThat(entries).isEqualTo(new SimpleEntryList<>());
    }

    @Test
    void testSingleElement() {
        SimpleEntryList<Integer, String> entries = new SimpleEntryList<>();
        entries.add(4, "lorem");
        SimpleEntryList<Integer, String> entriesDouble = new SimpleEntryList<>();
        entriesDouble.add(4, "lorem");
        assertThat(entries).isNotEmpty();
        assertThat(entries.isEmpty()).isFalse();
        assertThat(entries.size()).isEqualTo(1);
        assertThat(entries.iterator()).hasNext();
        assertThat(entries).containsExactly(SimpleEntryList.Entry.of(4, "lorem"));
        assertThat(entries.last()).isEqualTo(SimpleEntryList.Entry.of(4, "lorem"));
        assertThat(entries).isEqualTo(entriesDouble);
        entries.removeLast();
        assertThat(entries).isEmpty();
        assertThat(entries.isEmpty()).isTrue();
        assertThat(entries.size()).isZero();
        assertThat(entries.iterator()).isExhausted();
        assertThatThrownBy(entries::last).isInstanceOf(NoSuchElementException.class);
        assertThatThrownBy(entries::removeLast).isInstanceOf(NoSuchElementException.class);
        assertThat(entries).isEqualTo(new SimpleEntryList<>());
    }

    @Test
    void testMultipleElements() {
        SimpleEntryList<Integer, String> entries = new SimpleEntryList<>();
        entries.add(2, "lorem");
        entries.add(3, "ipsum");
        entries.add(4, "dolor");
        SimpleEntryList<Integer, String> entriesDouble = new SimpleEntryList<>();
        entriesDouble.add(2, "lorem");
        entriesDouble.add(3, "ipsum");
        entriesDouble.add(4, "dolor");
        assertThat(entries).isNotEmpty();
        assertThat(entries.isEmpty()).isFalse();
        assertThat(entries.size()).isEqualTo(3);
        assertThat(entries.iterator()).hasNext();
        assertThat(entries).containsExactly(
                SimpleEntryList.Entry.of(2, "lorem"),
                SimpleEntryList.Entry.of(3, "ipsum"),
                SimpleEntryList.Entry.of(4, "dolor"));
        assertThat(entries.last()).isEqualTo(SimpleEntryList.Entry.of(4, "dolor"));
        assertThat(entries).isEqualTo(entriesDouble);
        entries.removeLast();
        assertThat(entries.isEmpty()).isFalse();
        assertThat(entries.size()).isEqualTo(2);
        assertThat(entries.last()).isEqualTo(SimpleEntryList.Entry.of(3, "ipsum"));
        entries.removeLast();
        assertThat(entries.isEmpty()).isFalse();
        assertThat(entries.size()).isEqualTo(1);
        assertThat(entries.last()).isEqualTo(SimpleEntryList.Entry.of(2, "lorem"));
        entries.removeLast();
        assertThat(entries).isEmpty();
        assertThat(entries.isEmpty()).isTrue();
        assertThat(entries.size()).isZero();
        assertThatThrownBy(entries::last).isInstanceOf(NoSuchElementException.class);
        assertThatThrownBy(entries::removeLast).isInstanceOf(NoSuchElementException.class);
    }

}
