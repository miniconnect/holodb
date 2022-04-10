package hu.webarticum.holodb.storage;

import java.math.BigInteger;
import java.util.Iterator;

import hu.webarticum.holodb.core.data.selection.Selection;
import hu.webarticum.miniconnect.rdmsframework.storage.OrderKey;
import hu.webarticum.miniconnect.rdmsframework.storage.TableSelection;

public class SelectionTableSelection implements TableSelection {
    
    private final Selection selection;
    
    private final OrderKey ascOrderKey;
    
    private final OrderKey descOrderKey;
    
    private final boolean ascOrder;
    

    public SelectionTableSelection(Selection selection, boolean ascOrder) {
        this(selection, OrderKey.adHoc(), OrderKey.adHoc(), ascOrder);
    }
    
    public SelectionTableSelection(
            Selection selection,
            OrderKey ascOrderKey,
            OrderKey descOrderKey,
            boolean ascOrder) {
        this.selection = selection;
        this.ascOrderKey = ascOrderKey;
        this.descOrderKey = descOrderKey;
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
    public OrderKey orderKey() {
        return ascOrder ? ascOrderKey : descOrderKey;
    }

    @Override
    public TableSelection reversed() {
        return new SelectionTableSelection(selection, ascOrderKey, descOrderKey, !ascOrder);
    }
    
}
