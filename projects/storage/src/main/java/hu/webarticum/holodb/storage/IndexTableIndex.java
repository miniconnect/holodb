package hu.webarticum.holodb.storage;

import hu.webarticum.holodb.core.data.selection.Selection;
import hu.webarticum.holodb.core.data.source.Index;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.minibase.storage.api.SingleColumnTableIndex;
import hu.webarticum.minibase.storage.api.TableSelection;
import hu.webarticum.minibase.storage.impl.compound.DisjunctUnionTableSelection;
import hu.webarticum.minibase.storage.impl.simple.SimpleSelection;

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
        boolean ascOrder = sortMode.isAsc();
        boolean nullsFirst = sortMode.isNullsFirst();

        if (nullsMode == NullsMode.NULLS_ONLY) {
            if (
                    (nullsFirst && from == null) ||
                    (!nullsFirst && to == null)) {
                return new SelectionTableSelection(index.findNulls(), ascOrder);
            } else {
                return new SimpleSelection(ImmutableList.empty());
            }
        }

        Selection mainSelection = index.findBetween(
                from,
                fromInclusionMode == InclusionMode.INCLUDE,
                to,
                toInclusionMode == InclusionMode.INCLUDE);
        TableSelection mainTableSelection = new SelectionTableSelection(mainSelection, ascOrder);
        if (
                nullsMode == NullsMode.NO_NULLS ||
                (nullsFirst && from != null) ||
                (!nullsFirst && to != null)) {
            return mainTableSelection;
        }

        Selection nullsSelection = index.findNulls();
        TableSelection nullsTableSelection = new SelectionTableSelection(nullsSelection, ascOrder);

        if (nullsFirst) {
            return DisjunctUnionTableSelection.of(nullsTableSelection, mainTableSelection);
        } else {
            return DisjunctUnionTableSelection.of(mainTableSelection, nullsTableSelection);
        }
    }

}
