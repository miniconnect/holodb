package hu.webarticum.holodb.app.factory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.mifmif.common.regex.Generex;

import hu.webarticum.holodb.app.config.HoloConfig;
import hu.webarticum.holodb.app.config.HoloConfigColumn;
import hu.webarticum.holodb.app.config.HoloConfigSchema;
import hu.webarticum.holodb.app.config.HoloConfigTable;
import hu.webarticum.holodb.app.config.HoloConfigColumn.ColumnMode;
import hu.webarticum.holodb.app.launch.HolodbServerMain;
import hu.webarticum.holodb.app.misc.GenerexSource;
import hu.webarticum.holodb.app.misc.StrexSource;
import hu.webarticum.holodb.core.data.binrel.monotonic.BinomialMonotonic;
import hu.webarticum.holodb.core.data.binrel.permutation.DirtyFpePermutation;
import hu.webarticum.holodb.core.data.binrel.permutation.Permutation;
import hu.webarticum.holodb.core.data.random.HasherTreeRandom;
import hu.webarticum.holodb.core.data.random.TreeRandom;
import hu.webarticum.holodb.core.data.source.FixedSource;
import hu.webarticum.holodb.core.data.source.Index;
import hu.webarticum.holodb.core.data.source.IndexedSource;
import hu.webarticum.holodb.core.data.source.MonotonicSource;
import hu.webarticum.holodb.core.data.source.NullPaddedSortedSource;
import hu.webarticum.holodb.core.data.source.NullPaddedSource;
import hu.webarticum.holodb.core.data.source.PermutatedIndexedSource;
import hu.webarticum.holodb.core.data.source.PermutatedSource;
import hu.webarticum.holodb.core.data.source.RangeSource;
import hu.webarticum.holodb.core.data.source.SortedSource;
import hu.webarticum.holodb.core.data.source.Source;
import hu.webarticum.holodb.core.data.source.TransformingSortedSource;
import hu.webarticum.holodb.core.data.source.TransformingSource;
import hu.webarticum.holodb.core.data.source.UniqueSource;
import hu.webarticum.holodb.storage.GenericNamedResourceStore;
import hu.webarticum.holodb.storage.HoloSimpleSource;
import hu.webarticum.holodb.storage.HoloTable;
import hu.webarticum.holodb.storage.IndexTableIndex;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;
import hu.webarticum.miniconnect.rdmsframework.storage.ColumnDefinition;
import hu.webarticum.miniconnect.rdmsframework.storage.NamedResourceStore;
import hu.webarticum.miniconnect.rdmsframework.storage.Schema;
import hu.webarticum.miniconnect.rdmsframework.storage.StorageAccess;
import hu.webarticum.miniconnect.rdmsframework.storage.Table;
import hu.webarticum.miniconnect.rdmsframework.storage.TableIndex;
import hu.webarticum.miniconnect.rdmsframework.storage.impl.diff.DiffTable;
import hu.webarticum.miniconnect.rdmsframework.storage.impl.simple.SimpleColumnDefinition;
import hu.webarticum.miniconnect.rdmsframework.storage.impl.simple.SimpleResourceManager;
import hu.webarticum.miniconnect.rdmsframework.storage.impl.simple.SimpleSchema;
import hu.webarticum.miniconnect.rdmsframework.storage.impl.simple.SimpleStorageAccess;
import hu.webarticum.miniconnect.record.converter.Converter;
import hu.webarticum.strex.Strex;

// TODO: split to builder and sub-builders
public class StorageAccessFactory {

    public static StorageAccess createStorageAccess(HoloConfig config, Converter converter) {
        SimpleStorageAccess storageAccess =  new SimpleStorageAccess();
        SimpleResourceManager<Schema> schemaManager = storageAccess.schemas();
        TreeRandom rootRandom = new HasherTreeRandom(config.seed());
        for (HoloConfigSchema schemaConfig : config.schemas()) {
            Schema schema = createSchema(schemaConfig, rootRandom, converter);
            schemaManager.register(schema);
        }
        return storageAccess;
    }

    private static Schema createSchema(
            HoloConfigSchema schemaConfig,
            TreeRandom rootRandom,
            Converter converter) {
        String schemaName = schemaConfig.name();
        TreeRandom schemaRandom = rootRandom.sub("schema-" + schemaName);
        SimpleSchema schema = new SimpleSchema(schemaName);
        SimpleResourceManager<Table> tableManager = schema.tables();
        for (HoloConfigTable tableConfig : schemaConfig.tables()) {
            Table table = createTable(tableConfig, schemaRandom, converter);
            tableManager.register(table);
        }
        return schema;
    }
    
    private static Table createTable(
            HoloConfigTable tableConfig,
            TreeRandom schemaRandom,
            Converter converter) {
        BigInteger tableSize = tableConfig.size();
        String tableName = tableConfig.name();
        TreeRandom tableRandom = schemaRandom.sub("table-" + tableName);
        ImmutableList<HoloConfigColumn> columnConfigs = ImmutableList.fromCollection(tableConfig.columns());
        ImmutableList<String> columnNames = columnConfigs.map(HoloConfigColumn::name);
        ImmutableMap<String, Source<?>> columnSources = columnConfigs
                .assign(c -> createColumnSource(c, tableName, tableRandom, converter, tableSize))
                .map(HoloConfigColumn::name, s -> s);
        Optional<String> autoIncrementedName = extractAutoIncrementedColumnName(columnConfigs);
        ImmutableList<ColumnDefinition> columnDefinitions =
                columnConfigs.map(c -> new SimpleColumnDefinition(
                        c.type(),
                        !c.nullCount().equals(tableSize),
                        c.mode() == ColumnMode.COUNTER,
                        autoIncrementedName.isPresent() && c.name().equals(autoIncrementedName.get()),
                        extractEnumValues(c, columnSources.get(c.name())),
                        extractComparator(columnSources.get(c.name()))));
        NamedResourceStore<TableIndex> indexStore = createIndexStore(columnSources);
        BigInteger sequenceValue = autoIncrementedName.isPresent() ? tableSize.add(BigInteger.ONE) : BigInteger.ONE;
        Table table = new HoloTable(
                tableName,
                tableSize,
                columnNames,
                columnDefinitions,
                columnSources,
                ImmutableMap.empty(),
                indexStore,
                sequenceValue);
        if (tableConfig.writeable()) {
            table = new DiffTable(table);
        }
        return table;
    }
    
    private static ImmutableList<Object> extractEnumValues(HoloConfigColumn columnConfig, Source<?> source) {
        if (columnConfig.mode() != ColumnMode.ENUM) {
            return null;
        }
        
        return source.possibleValues().get().map(v -> v); // NOSONAR must be presented
    }

    private static Optional<String> extractAutoIncrementedColumnName(ImmutableList<HoloConfigColumn> columnConfigs) {
        for (HoloConfigColumn columnConfig : columnConfigs) {
            if (columnConfig.mode() == ColumnMode.COUNTER) {
                return Optional.of(columnConfig.name());
            }
        }
        
        return Optional.empty();
    }

    private static Source<?> createColumnSource(
            HoloConfigColumn columnConfig,
            String tableName,
            TreeRandom tableRandom,
            Converter converter,
            BigInteger tableSize) {
        ColumnMode columnMode = columnConfig.mode();
        if (columnMode == ColumnMode.DEFAULT || columnMode == ColumnMode.ENUM) {
            boolean isEnum = (columnMode == ColumnMode.ENUM);
            TreeRandom columnRandom = tableRandom.sub("col-" + columnConfig.name());
            BigInteger nullCount = columnConfig.nullCount();
            String dynamicPattern =columnConfig.valuesDynamicPattern();
            if (dynamicPattern == null) {
                SortedSource<?> baseSource = loadBaseSource(columnConfig, converter, isEnum);
                return createDefaultSource(columnRandom, baseSource, tableSize, nullCount);
            } else if (!isEnum) {
                return createDynamicPatternSource(columnConfig, columnRandom, converter, tableSize);
            } else {
                throw new IllegalArgumentException(
                        "ENUM mode can not be used with dynamic column (" +
                        tableName + "." + columnConfig.name() + ")");
            }
        } else if (columnMode == ColumnMode.COUNTER) {
            return new RangeSource(BigInteger.ONE, tableSize);
        } else if (columnMode == ColumnMode.FIXED) {
            return createFixedSource(columnConfig.type(), columnConfig.values());
        } else {
            throw new IllegalArgumentException(
                    "Invalid column mode: " + columnMode + " (" + tableName + "." + columnConfig.name() + ")");
        }
    }

    private static SortedSource<?> loadBaseSource(HoloConfigColumn columnConfig, Converter converter, boolean isEnum) {
        if (columnConfig.valuesRange() != null) {
            return loadRangeSource(columnConfig, converter);
        } else if (columnConfig.valuesPattern() != null) {
            return loadPatternSource(columnConfig, converter);
        }
        
        List<Object> values = loadValues(columnConfig, converter);
        return createUniqueSource(columnConfig.type(), values, isEnum);
    }

    private static SortedSource<?> loadRangeSource(HoloConfigColumn columnConfig, Converter converter) {
        List<BigInteger> valuesRange = columnConfig.valuesRange();
        Class<?> type = columnConfig.type();
        BigInteger from = valuesRange.get(0);
        BigInteger to = valuesRange.get(1);
        BigInteger size = to.subtract(from).add(BigInteger.ONE);
        RangeSource rangeSource = new RangeSource(from, size);
        if (type == BigInteger.class) {
            return rangeSource;
        }
        
        if (type == BigInteger.class) {
            return rangeSource;
        } else {
            return new TransformingSortedSource<BigInteger, Object>( // NOSONAR explicit type parameters are necessary
                    rangeSource,
                    type,
                    v -> (BigInteger) converter.convert(v, BigInteger.class),
                    b -> converter.convert(b, type));
        }
    }

    private static SortedSource<?> loadPatternSource(HoloConfigColumn columnConfig, Converter converter) {
        StrexSource strexSource = new StrexSource(Strex.compile(columnConfig.valuesPattern()));
        Class<?> type = columnConfig.type();
        if (type == String.class) {
            return strexSource;
        } else {
            return new TransformingSortedSource<String, Object>( // NOSONAR explicit type parameters are necessary
                    strexSource,
                    type,
                    v -> (String) converter.convert(v, String.class),
                    b -> converter.convert(b, type));
        }
    }

    private static List<Object> loadValues(HoloConfigColumn columnConfig, Converter converter) {
        Class<?> columnClazz = columnConfig.type();
        List<Object> rawValues = loadRawValues(columnConfig, converter);
        return rawValues.stream()
                .map(v -> converter.convert(v, columnClazz))
                .collect(Collectors.toList());
    }
    
    private static List<Object> loadRawValues(HoloConfigColumn columnConfig, Converter converter) {
        String valuesResource = columnConfig.valuesResource();
        if (valuesResource != null) {
            return loadValuesFromResource(valuesResource);
        }
        
        return columnConfig.values();
    }
    
    private static List<Object> loadValuesFromResource(String resource) {
        ClassLoader classLoader = HolodbServerMain.class.getClassLoader();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                classLoader.getResourceAsStream(resource)))) {
            List<Object> values = new LinkedList<>();
            String value;
            while ((value = reader.readLine()) != null) {
                if (!value.isEmpty()) {
                    values.add(value);
                }
            }
            return values;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Comparator<?> extractComparator(Source<?> source) {
        if (!(source instanceof Index)) {
            return null;
        }
        
        Index index = (Index) source;
        return index.comparator();
    }

    private static UniqueSource<?> createUniqueSource(Class<?> type, Collection<?> values, boolean isEnum) {
        Comparator<?> comparator = isEnum ? createEnumValueComparator(values) : null;
        try {
            return UniqueSource.class
                    .getConstructor(Class.class, Collection.class, Comparator.class)
                    .newInstance(type, values, comparator);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
    
    private static Comparator<?> createEnumValueComparator(Collection<?> values) {
        Map<Object, Integer> positionMap = new HashMap<>(values.size());
        int position = 0;
        for (Object value : values) {
            if (positionMap.containsKey(value)) {
                throw new IllegalArgumentException("Duplicated ENUM value: " + value);
            }
            positionMap.put(value, position);
            position++;
        }
        return (v1, v2) -> compareEnumValues(v1, v2, positionMap);
    }

    private static int compareEnumValues(Object value1, Object value2, Map<Object, Integer> positionMap) {
        Integer position1 = positionMap.get(value1);
        Integer position2 = positionMap.get(value2);
        if (position1 == null) {
            if (position2 == null) {
                return 0;
            } else {
                return -1;
            }
        } else if (position2 == null) {
            return 1;
        }
        return position1.compareTo(position2);
    }
    
    private static <T> IndexedSource<T> createDefaultSource(
            TreeRandom treeRandom, SortedSource<T> baseSource, BigInteger tableSize, BigInteger nullCount) {
        BigInteger valueCount = tableSize.subtract(nullCount);
        SortedSource<T> valueSource = new MonotonicSource<>(
                baseSource,
                new BinomialMonotonic(treeRandom.sub("monotonic"), valueCount, baseSource.size()));
        if (!valueCount.equals(tableSize)) {
            valueSource = new NullPaddedSortedSource<>(valueSource, tableSize);
        }
        Permutation permutation = new DirtyFpePermutation(treeRandom.sub("permutation"), tableSize);
        return new PermutatedIndexedSource<>(valueSource, permutation);
    }
    
    private static Source<?> createDynamicPatternSource(
            HoloConfigColumn columnConfig,
            TreeRandom columnRandom,
            Converter converter,
            BigInteger tableSize) {
        BigInteger nullCount = columnConfig.nullCount();
        BigInteger valueCount = tableSize.subtract(nullCount);
        String dynamicPattern = columnConfig.valuesDynamicPattern();
        Class<?> type = columnConfig.type();
        GenerexSource generexSource = new GenerexSource(new Generex(dynamicPattern), columnRandom, valueCount);
        Source<?> source = generexSource;
        if (type != String.class) {
            source = new TransformingSource<String, Object>( // NOSONAR explicit type parameters are necessary
                    generexSource,
                    type,
                    b -> converter.convert(b, type));
        }
        if (!nullCount.equals(BigInteger.ZERO)) {
            source = new NullPaddedSource<>(source, tableSize);
            Permutation permutation = new DirtyFpePermutation(columnRandom.sub("permutation"), tableSize);
            source = new PermutatedSource<>(source, permutation);
        }
        return source;
    }
    
    private static FixedSource<?> createFixedSource(Class<?> type, Collection<?> values) {
        try {
            return FixedSource.class.getConstructor(Class.class, Collection.class)
                    .newInstance(type, values);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static NamedResourceStore<TableIndex> createIndexStore(
            ImmutableMap<String, Source<?>> columnSources) {
        List<TableIndex> tableIndexes = new ArrayList<>();
        for (Map.Entry<String, Source<?>> entry : columnSources.entrySet()) {
            String columnName = entry.getKey();
            String indexName = "idx_" + columnName;
            Source<?> source = entry.getValue();
            if (source instanceof HoloSimpleSource) {
                TableIndex tableIndex = ((HoloSimpleSource<?>) source)
                        .createIndex(indexName, columnName);
                tableIndexes.add(tableIndex);
            } else if (source instanceof IndexedSource) {
                TableIndex tableIndex = new IndexTableIndex(indexName, columnName, (Index) source);
                tableIndexes.add(tableIndex);
            }
        }
        return GenericNamedResourceStore.from(tableIndexes);
    }

}
