package hu.webarticum.holodb.core.query.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections4.iterators.UnmodifiableIterator;

public final class GroupByPart implements Iterable<Expression> {

    private final List<Expression> items;
    
    
    public GroupByPart(Expression... items) {
        this(items == null ? null : Arrays.asList(items));
    }

    public GroupByPart(Collection<Expression> items) {
        this.items = new ArrayList<>(Objects.requireNonNull(items, "Collection of groupings can not be null"));
    }
    
    
    public int size() {
        return items.size();
    }
    
    public Expression at(int index) {
        return items.get(index);
    }

    public List<Expression> toList() {
        return new ArrayList<>(items);
    }

    @Override
    public Iterator<Expression> iterator() {
        return UnmodifiableIterator.unmodifiableIterator(items.iterator());
    }
    
    @Override
    public int hashCode() {
        return items.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GroupByPart)) {
            return false;
        }
        
        return items.equals(((GroupByPart) obj).items);
    }
    
}
