package hu.webarticum.holodb.storage;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.NoSuchElementException;

import hu.webarticum.holodb.core.data.selection.Selection;
import hu.webarticum.miniconnect.rdmsframework.storage.OrderKey;
import hu.webarticum.miniconnect.rdmsframework.storage.TableSelection;
import hu.webarticum.miniconnect.rdmsframework.storage.TableSelectionEntry;

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
    public Iterator<TableSelectionEntry> iterator() {
        return new SelectionTableSelectionIterator();
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
    
    
    private class SelectionTableSelectionIterator implements Iterator<TableSelectionEntry> {

        private final BigInteger size = selection.size();
        
        private BigInteger nextOrderIndex = BigInteger.ZERO;
        
        
        @Override
        public boolean hasNext() {
            return nextOrderIndex.compareTo(size) < 0;
        }

        @Override
        public TableSelectionEntry next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            
            BigInteger selectionIndex;
            OrderKey orderKey;
            if (ascOrder) {
                selectionIndex = nextOrderIndex;
                orderKey = ascOrderKey;
            } else {
                selectionIndex = size.subtract(BigInteger.ONE).subtract(nextOrderIndex);
                orderKey = descOrderKey;
            }
            
            BigInteger rowIndex = selection.at(selectionIndex);
            TableSelectionEntry entry = new TableSelectionEntry(orderKey, rowIndex, nextOrderIndex);
            
            nextOrderIndex = nextOrderIndex.add(BigInteger.ONE);
            
            return entry;
        }
        
    }
    
}
