package hu.webarticum.holodb.storage;

import java.math.BigInteger;
import java.util.Iterator;

import hu.webarticum.holodb.core.data.selection.Selection;
import hu.webarticum.miniconnect.rdmsframework.storage.TableSelection;

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
    public Iterator<BigInteger> iterator() {
        return selection.iterator();
    }

    @Override
    public boolean containsRow(BigInteger rowIndex) {
        return selection.contains(rowIndex);
    }

    @Override
    public TableSelection reversed() {
        return new SelectionTableSelection(selection, !ascOrder);
    }
    
}
