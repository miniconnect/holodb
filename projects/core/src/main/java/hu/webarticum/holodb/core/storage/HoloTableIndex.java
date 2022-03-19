package hu.webarticum.holodb.core.storage;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.rdmsframework.storage.TableIndex;
import hu.webarticum.miniconnect.rdmsframework.storage.TableSelection;

public class HoloTableIndex implements TableIndex {
    
    private final String name;
    
    private final ImmutableList<String> columnNames;
    
    private final StandaloneIndex standaloneIndex;
    
    
    public HoloTableIndex(
            String name, ImmutableList<String> columnNames, StandaloneIndex standaloneIndex) {
        this.name = name;
        this.columnNames = columnNames;
        this.standaloneIndex = standaloneIndex;
    }
    

    @Override
    public String name() {
        return name;
    }

    @Override
    public ImmutableList<String> columnNames() {
        return columnNames;
    }
    
    public StandaloneIndex standaloneIndex() {
        return standaloneIndex;
    }

    @Override
    public boolean isUnique() {
        return standaloneIndex.isUnique();
    }

    @Override
    public TableSelection find(
            ImmutableList<?> from,
            boolean fromInclusive,
            ImmutableList<?> to,
            boolean toInclusive,
            boolean sort) {
        return standaloneIndex.find(from, fromInclusive, to, toInclusive, sort);
    }

}
