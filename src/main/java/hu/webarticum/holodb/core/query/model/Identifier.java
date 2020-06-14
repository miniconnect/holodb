package hu.webarticum.holodb.core.query.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections4.iterators.UnmodifiableIterator;

public final class Identifier implements Expression, Iterable<String> {

    private final List<String> nameParts;

    private final String baseName;
    
    
    public Identifier(String... nameParts) {
        this(nameParts == null ? null : Arrays.asList(nameParts));
    }
    
    public Identifier(Collection<String> nameParts) {
        Objects.requireNonNull(nameParts, "Collection of name parts can not be null");
        
        int partsCount = nameParts.size();
        if (partsCount < 1) {
            throw new IllegalArgumentException("Collection of name parts can not be empty");
        }
        
        this.nameParts = new ArrayList<>(nameParts);
        this.baseName = this.nameParts.get(partsCount - 1);
    }
    
    
    public int size() {
        return nameParts.size();
    }
    
    public String at(int index) {
        return nameParts.get(index);
    }

    public List<String> toList() {
        return new ArrayList<>(nameParts);
    }

    @Override
    public Iterator<String> iterator() {
        return UnmodifiableIterator.unmodifiableIterator(nameParts.iterator());
    }
    
    public String getBaseName() {
        return baseName;
    }
    
    @Override
    public int hashCode() {
        return nameParts.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Identifier)) {
            return false;
        }
        
        return nameParts.equals(((Identifier) obj).nameParts);
    }
    
}
