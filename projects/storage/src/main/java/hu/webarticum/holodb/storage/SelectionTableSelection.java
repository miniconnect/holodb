package hu.webarticum.holodb.storage;

import java.util.Iterator;

import hu.webarticum.holodb.core.data.selection.Selection;
import hu.webarticum.minibase.storage.api.TableSelection;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class SelectionTableSelection implements TableSelection {
    
    private final Selection selection;
    
    private final boolean ascOrder;
    

    public SelectionTableSelection(Selection selection, boolean ascOrder) {
        this.selection = selection;
        this.ascOrder = ascOrder;
    }
    
    
    public Selection selection() {
        return selection;
    }

    @Override
    public Iterator<LargeInteger> iterator() {
        return ascOrder ? selection.iterator() : selection.reverseOrder().iterator();
    }

    @Override
    public boolean containsRow(LargeInteger rowIndex) {
        return selection.contains(rowIndex);
    }

    public SelectionTableSelection reversed() {
        return new SelectionTableSelection(selection, !ascOrder);
    }
    
}
