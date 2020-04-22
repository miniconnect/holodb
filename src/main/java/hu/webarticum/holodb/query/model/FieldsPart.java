package hu.webarticum.holodb.query.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections4.iterators.UnmodifiableIterator;

public final class FieldsPart implements Iterable<FieldsItem> {

    private final List<FieldsItem> items;
    
    
    public FieldsPart(FieldsItem... items) {
        this(items == null ? null : Arrays.asList(items));
    }

    public FieldsPart(Collection<FieldsItem> items) {
        this.items = new ArrayList<>(Objects.requireNonNull(items, "Collection of fields can not be null"));
    }
    
    
    public int size() {
        return items.size();
    }
    
    public FieldsItem at(int index) {
        return items.get(index);
    }

    public List<FieldsItem> toList() {
        return new ArrayList<>(items);
    }

    @Override
    public Iterator<FieldsItem> iterator() {
        return UnmodifiableIterator.unmodifiableIterator(items.iterator());
    }
    
    @Override
    public int hashCode() {
        return items.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FieldsPart)) {
            return false;
        }
        
        return items.equals(((FieldsPart) obj).items);
    }
    
}
