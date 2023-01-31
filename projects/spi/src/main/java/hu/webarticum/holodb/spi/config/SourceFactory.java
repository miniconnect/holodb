package hu.webarticum.holodb.spi.config;

import hu.webarticum.holodb.core.data.random.TreeRandom;
import hu.webarticum.holodb.core.data.source.Source;
import hu.webarticum.miniconnect.lang.LargeInteger;

public interface SourceFactory {

    public Source<?> create( // NOSONAR wildcard is OK
            ColumnLocation columnLocation, TreeRandom treeRandom, LargeInteger size, Object data);
    
}
