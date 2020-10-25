package hu.webarticum.holodb.query.dummy;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

import hu.webarticum.holodb.query.common.ResultRow;
import hu.webarticum.holodb.query.common.ResultSet;

public class DummyResultSet implements ResultSet {
    
    private final int size;
    
    private final Function<Integer, ResultRow> rowFactory;
    

    public DummyResultSet(int size, Function<Integer, ResultRow> rowFactory) {
        this.size = size;
        this.rowFactory = rowFactory;
    }
    
    @Override
    public Iterator<ResultRow> iterator() {
        return new DummyResultSetIterator();
    }
    
    
    private class DummyResultSetIterator implements Iterator<ResultRow> {

        private int counter = 0;
        
        @Override
        public boolean hasNext() {
            return counter < size;
        }

        @Override
        public ResultRow next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            
            ResultRow row = rowFactory.apply(counter);
            
            counter++;
            
            return row;
        }
        
    }

}
