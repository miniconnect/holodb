package hu.webarticum.holodb.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections4.iterators.UnmodifiableIterator;

public class OrderByPart implements Iterable<OrderByItem> {

    private final List<OrderByItem> items = new ArrayList<>();
    
    
    public OrderByPart(OrderByItem... items) {
        this(Arrays.asList(items));
    }

    public OrderByPart(Collection<OrderByItem> items) {
        items.addAll(items);
    }
    
    
    public int size() {
        return items.size();
    }
    
    public OrderByItem at(int index) {
        return items.get(index);
    }

    public List<OrderByItem> toList() {
        return new ArrayList<>(items);
    }

    @Override
    public Iterator<OrderByItem> iterator() {
        return UnmodifiableIterator.unmodifiableIterator(items.iterator());
    }
    
    @Override
    public int hashCode() {
        return items.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof OrderByPart)) {
            return false;
        }
        
        return items.equals(((OrderByPart) obj).items);
    }
    
}
