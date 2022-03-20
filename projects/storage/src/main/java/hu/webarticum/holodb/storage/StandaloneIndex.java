package hu.webarticum.holodb.storage;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.rdmsframework.storage.TableSelection;

public interface StandaloneIndex {

    public int width();

    public boolean isUnique();

    public TableSelection find(
            ImmutableList<?> from,
            boolean fromInclusive,
            ImmutableList<?> to,
            boolean toInclusive,
            boolean sort);

    public default TableSelection find(ImmutableList<?> values) {
        return find(values, true, values, true, false);
    }
    
    public default TableSelection findValue(Object value) {
        return find(ImmutableList.of(value));
    }
    
}
