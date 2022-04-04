package hu.webarticum.holodb.storage;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import hu.webarticum.holodb.core.data.source.Source;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;
import hu.webarticum.miniconnect.rdmsframework.storage.Column;
import hu.webarticum.miniconnect.rdmsframework.storage.ColumnDefinition;
import hu.webarticum.miniconnect.rdmsframework.storage.NamedResourceStore;
import hu.webarticum.miniconnect.rdmsframework.storage.Row;
import hu.webarticum.miniconnect.rdmsframework.storage.Table;
import hu.webarticum.miniconnect.rdmsframework.storage.TableIndex;
import hu.webarticum.miniconnect.rdmsframework.storage.TablePatch;

public class HoloTable implements Table {
    
    private final String name;
    
    private final BigInteger size;
    
    private final ImmutableList<String> columnNames;
    
    private final ImmutableMap<String, ? extends Source<?>> singleColumnSources;
    
    private final ImmutableMap<String, MultiColumnSourceEntry> multiColumnSourceMap;
    
    private final NamedResourceStore<Column> columnStore;
    
    private final NamedResourceStore<TableIndex> indexStore;
    
    
    public HoloTable(
            String name,
            BigInteger size,
            ImmutableList<String> columnNames,
            ImmutableList<? extends ColumnDefinition> columnDefinitions,
            ImmutableMap<String, ? extends Source<?>> singleColumnSources,
            ImmutableMap<ImmutableList<String>, ? extends Source<? extends ImmutableList<?>>>
                    multiColumnSources,
            NamedResourceStore<TableIndex> indexStore) {
        checkSources(
                size,
                columnNames,
                columnDefinitions,
                singleColumnSources,
                multiColumnSources,
                indexStore);
        this.name = name;
        this.size = size;
        this.columnNames = columnNames;
        this.singleColumnSources = singleColumnSources;
        this.multiColumnSourceMap = buildMultiColumnSourceMap(multiColumnSources);
        this.columnStore = buildColumnStore(
                columnNames, columnDefinitions, singleColumnSources, multiColumnSources);
        this.indexStore = indexStore;
    }
    
    private static void checkSources(
            BigInteger size,
            ImmutableList<String> columnNames,
            ImmutableList<? extends ColumnDefinition> columnDefinitions,
            ImmutableMap<String, ? extends Source<?>> singleColumnSources,
            ImmutableMap<ImmutableList<String>, ? extends Source<? extends ImmutableList<?>>>
                    multiColumnSources,
            NamedResourceStore<TableIndex> indexStore) {
        int width = columnNames.size();
        int numberOfColumnDefinitions = columnDefinitions.size();
        if (numberOfColumnDefinitions != width) {
            throw new IllegalArgumentException(String.format(
                    "Unexpected number of column definitions: %d (table width: %d)",
                    numberOfColumnDefinitions,
                    width));
        }
        Set<String> columnNamesFromSources = new HashSet<>(singleColumnSources.keySet());
        for (ImmutableList<String> sourceColumnNames : multiColumnSources.keySet()) {
            for (String columnName : sourceColumnNames) {
                if (columnNamesFromSources.contains(columnName)) {
                    throw new IllegalArgumentException("Duplicate column name: " + columnName);
                }
            }
        }
        Set<String> columnNameSet = new HashSet<>(columnNames.asList());
        if (!columnNameSet.equals(columnNamesFromSources)) {
            Set<String> missingColumnNames = new HashSet<>(columnNameSet);
            missingColumnNames.removeAll(columnNamesFromSources);
            Set<String> unexpectedColumnNames = new HashSet<>(columnNamesFromSources);
            unexpectedColumnNames.removeAll(columnNameSet);
            throw new IllegalArgumentException(String.format(
                    "Unmatching columns, missing: %s, unexpected: %s",
                    missingColumnNames,
                    unexpectedColumnNames));
        }
        checkSizes(singleColumnSources, size);
        checkSizes(multiColumnSources, size);
        for (TableIndex tableIndex : indexStore.resources()) {
            for (String columnName : tableIndex.columnNames()) {
                if (!columnNames.contains(columnName)) {
                    throw new IllegalArgumentException("Unexpected index column: " + columnName);
                }
            }
        }
    }

    private static void checkSizes(ImmutableMap<?, ? extends Source<?>> sources, BigInteger size) {
        for (Map.Entry<?, ? extends Source<?>> entry : sources.entrySet()) {
            Source<?> source = entry.getValue();
            BigInteger sourceSize = source.size();
            if (!sourceSize.equals(size)) {
                throw new IllegalArgumentException(String.format(
                        "Unmatching size for %s: %d, expected: %d",
                        entry.getKey().toString(),
                        sourceSize,
                        size));
            }
        }
        
    }

    private static ImmutableMap<String, MultiColumnSourceEntry> buildMultiColumnSourceMap(
            ImmutableMap<ImmutableList<String>, ? extends Source<? extends ImmutableList<?>>>
                    multiColumnSources) {
        Map<String, MultiColumnSourceEntry> resultBuilder = new HashMap<>();
        for (Map.Entry<ImmutableList<String>, ? extends Source<? extends ImmutableList<?>>> entry :
                multiColumnSources.entrySet()) {
            ImmutableList<String> columnNames = entry.getKey();
            Source<? extends ImmutableList<?>> source = entry.getValue();
            MultiColumnSourceEntry multiColumnSourceEntry =
                    new MultiColumnSourceEntry(columnNames, source);
            for (String columnName : columnNames) {
                resultBuilder.put(columnName, multiColumnSourceEntry);
            }
        }
        return ImmutableMap.fromMap(resultBuilder);
    }

    private static NamedResourceStore<Column> buildColumnStore(
            ImmutableList<String> columnNames,
            ImmutableList<? extends ColumnDefinition> columnDefinitions,
            ImmutableMap<String, ? extends Source<?>> singleColumnSources,
            ImmutableMap<ImmutableList<String>, ? extends Source<? extends ImmutableList<?>>>
                    multiColumnSources) {
        Column[] columns = new Column[columnNames.size()];
        for (Map.Entry<String, ? extends Source<?>> entry : singleColumnSources.entrySet()) {
            String columnName = entry.getKey();
            Source<?> source = entry.getValue();
            int columnIndex = columnNames.indexOf(columnName);
            ColumnDefinition columnDefinition = columnDefinitions.get(columnIndex);
            Column column = new HoloSimpleColumn(columnName, columnDefinition, source);
            columns[columnIndex] = column;
        }
        for (Map.Entry<ImmutableList<String>, ? extends Source<? extends ImmutableList<?>>> entry :
                multiColumnSources.entrySet()) {
            ImmutableList<String> sourceColumnNames = entry.getKey();
            Source<? extends ImmutableList<?>> source = entry.getValue();
            int sourceWidth = sourceColumnNames.size();
            for (int i = 0; i < sourceWidth; i++) {
                String columnName = sourceColumnNames.get(i);
                int columnIndex = columnNames.indexOf(columnName);
                ColumnDefinition columnDefinition = columnDefinitions.get(columnIndex);
                Column column = new HoloPositionedColumn(columnName, columnDefinition, source, i);
                columns[columnIndex] = column;
            }
            
        }
        return GenericNamedResourceStore.of(columns);
    }


    @Override
    public String name() {
        return name;
    }

    @Override
    public NamedResourceStore<Column> columns() {
        return columnStore;
    }

    @Override
    public NamedResourceStore<TableIndex> indexes() {
        return indexStore;
    }

    @Override
    public BigInteger size() {
        return size;
    }

    @Override
    public Row row(BigInteger rowIndex) {
        return new HoloTableRow(rowIndex);
    }

    @Override
    public boolean isWritable() {
        return false;
    }

    @Override
    public void applyPatch(TablePatch patch) {
        throw new UnsupportedOperationException("This table is read-only");
    }
    
    
    private static class MultiColumnSourceEntry {
        
        private final ImmutableList<String> columnNames;
        
        private final Source<? extends ImmutableList<?>> source;
        
        
        private MultiColumnSourceEntry(
                ImmutableList<String> columnNames, Source<? extends ImmutableList<?>> source) {
            this.columnNames = columnNames;
            this.source = source;
        }
        
    }
    
    
    public class HoloTableRow implements Row {
        
        private final BigInteger rowIndex;
        
        
        private final Map<String, ValueCacheEntry> valueCache = new HashMap<>();
        
        
        private HoloTableRow(BigInteger rowIndex) {
            this.rowIndex = rowIndex;
        }
        

        @Override
        public ImmutableList<String> columnNames() {
            return columnNames;
        }

        @Override
        public Object get(int columnPosition) {
            return get(columnNames.get(columnPosition));
        }

        @Override
        public synchronized Object get(String columnName) {
            ValueCacheEntry currentCacheEntry = valueCache.get(columnName);
            if (currentCacheEntry != null) {
                return currentCacheEntry.value;
            }
            
            Source<?> singleColumnSource = singleColumnSources.get(columnName);
            if (singleColumnSource != null) {
                Object value = singleColumnSource.get(rowIndex);
                valueCache.put(columnName, new ValueCacheEntry(value));
                return value;
            }
            
            MultiColumnSourceEntry multiColumnSourceEntry = multiColumnSourceMap.get(columnName);
            ImmutableList<?> values = multiColumnSourceEntry.source.get(rowIndex);
            int sourceWidth = values.size();
            for (int i = 0; i < sourceWidth; i++) {
                String sourceColumnName = multiColumnSourceEntry.columnNames.get(i);
                Object value = values.get(i);
                valueCache.put(sourceColumnName, new ValueCacheEntry(value));
            }
            return valueCache.get(columnName);
        }

        @Override
        public ImmutableList<Object> getAll() {
            return columnNames.map(this::get);
        }

        @Override
        public ImmutableMap<String, Object> getMap() {
            return getMap(columnNames);
        }

        @Override
        public ImmutableMap<String, Object> getMap(ImmutableList<String> columnNames) {
            return columnNames.assign(this::get);
        }
        
    }
    
    
    private static class ValueCacheEntry {
        
        private final Object value;
        
        
        public ValueCacheEntry(Object value) {
            this.value = value;
        }
        
    }

}
