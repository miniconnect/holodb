package hu.webarticum.holodb.regex.charclass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import hu.webarticum.miniconnect.lang.ToStringBuilder;

public class SimpleEntryList<K, V> implements Iterable<SimpleEntryList.Entry<K, V>> {

    private final List<SimpleEntryList.Entry<K, V>> entries = new ArrayList<>();

    public void add(K key, V value) {
        entries.add(new Entry<>(key, value));
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        return Collections.unmodifiableList(entries).iterator();
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public int size() {
        return entries.size();
    }

    public Entry<K, V> last() {
        if (entries.isEmpty()) {
            throw new NoSuchElementException("Can't get the last element because there are no elements");
        }
        return entries.get(entries.size() - 1);
    }

    public void removeLast() {
        if (entries.isEmpty()) {
            throw new NoSuchElementException("Can't remove the last element because there are no elements");
        }
        entries.remove(entries.size() - 1);
    }

    @Override
    public int hashCode() {
        return entries.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof SimpleEntryList)) {
            return false;
        }
        SimpleEntryList<?, ?> otherEntrySet = (SimpleEntryList<?, ?>) obj;
        return entries.equals(otherEntrySet.entries);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("entries", entries)
                .build();
    }

    public static class Entry<K, V> {

        private final K key;

        private final V value;

        private Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public static <K, V> Entry<K, V> of(K key, V value) {
            return new Entry<>(key, value);
        }

        public K key() {
            return key;
        }

        public V value() {
            return value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, value);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (!(obj instanceof Entry)) {
                return false;
            }
            Entry<?, ?> otherEntry = (Entry<?, ?>) obj;
            return key.equals(otherEntry.key) && Objects.equals(value, otherEntry.value);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .add("key", key)
                    .add("value", value)
                    .build();
        }

    }

}
