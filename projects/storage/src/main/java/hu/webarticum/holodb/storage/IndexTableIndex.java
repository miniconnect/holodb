package hu.webarticum.holodb.storage;

import hu.webarticum.holodb.core.data.selection.Selection;
import hu.webarticum.holodb.core.data.source.Index;
import hu.webarticum.miniconnect.rdmsframework.storage.SingleColumnTableIndex;
import hu.webarticum.miniconnect.rdmsframework.storage.TableSelection;

/** TODO: improve this, see {@link Index} */
public class IndexTableIndex implements SingleColumnTableIndex {

    private final String name;
    
    private final String columnName;
    
    private final Index index;
    
    
    public IndexTableIndex(String name, String columnName, Index index) {
        this.name = name;
        this.columnName = columnName;
        this.index = index;
    }

    
    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean isUnique() {
        return false;
    }

    @Override
    public String columnName() {
        return columnName;
    }

    public Index index() {
        return index;
    }

    @Override
    public TableSelection find(
            Object from,
            InclusionMode fromInclusionMode,
            Object to,
            InclusionMode toInclusionMode,
            NullsMode nullsMode,
            SortMode sortMode) {
        boolean ascOrder =
                sortMode == SortMode.ASC_NULLS_FIRST ||
                sortMode == SortMode.ASC_NULLS_LAST;
        Selection selection = index.findBetween(
                from,
                fromInclusionMode == InclusionMode.INCLUDE,
                to,
                toInclusionMode == InclusionMode.INCLUDE);
        return new SelectionTableSelection(selection, ascOrder);
    }

}
