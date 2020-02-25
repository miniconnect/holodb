package hu.webarticum.holodb.util_old.path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class Path {
    
    private final List<PathEntry> entries;
    

    public Path(PathEntry... entries) {
        this(Arrays.asList(entries));
    }

    public Path(Collection<PathEntry> entries) {
        this.entries = new ArrayList<>(entries);
    }
    
    
    public static Path of(Object... entryValues) {
        List<PathEntry> entries = new ArrayList<>(entryValues.length);
        for (Object value : entryValues) {
            entries.add(PathEntry.of(value));
        }
        return new Path(entries);
    }
    
    
    public Iterator<PathEntry> iterator() {
        return entries.iterator();
    }

    public PathEntry at(int n) {
        return entries.get(n);
    }
    
    public int size() {
        return entries.size();
    }

    @Override
    public int hashCode() {
        return entries.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Path)) {
            return false;
        }
        
        Path otherPath = (Path) obj;
        
        if (otherPath.size() != entries.size()) {
            return false;
        }

        Iterator<PathEntry> iterator1 = iterator();
        Iterator<PathEntry> iterator2 = otherPath.iterator();
        
        while (iterator1.hasNext() && iterator2.hasNext()) {
            if (!iterator1.next().equals(iterator2.next())) {
                return false;
            }
        }
        
        return true;
    }
    
    @Override
    public String toString() {
        return entries.toString();
    }
    
}
