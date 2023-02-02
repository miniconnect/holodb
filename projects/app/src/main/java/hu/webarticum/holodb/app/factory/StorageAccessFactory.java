package hu.webarticum.holodb.app.factory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
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
import hu.webarticum.holodb.app.config.HoloConfigColumn.ShuffleQuality;
import hu.webarticum.holodb.app.launch.HolodbServerMain;
import hu.webarticum.holodb.app.misc.GenerexSource;
import hu.webarticum.holodb.app.misc.StrexSource;
import hu.webarticum.holodb.core.data.binrel.monotonic.BinomialMonotonic;
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
import hu.webarticum.holodb.spi.config.ColumnLocation;
import hu.webarticum.holodb.spi.config.SourceFactory;
import hu.webarticum.holodb.storage.GenericNamedResourceStore;
import hu.webarticum.holodb.storage.HoloSimpleSource;
import hu.webarticum.holodb.storage.HoloTable;
import hu.webarticum.holodb.storage.IndexTableIndex;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;
import hu.webarticum.miniconnect.lang.LargeInteger;
import hu.webarticum.minibase.storage.api.ColumnDefinition;
import hu.webarticum.minibase.storage.api.NamedResourceStore;
import hu.webarticum.minibase.storage.api.Schema;
import hu.webarticum.minibase.storage.api.StorageAccess;
import hu.webarticum.minibase.storage.api.Table;
import hu.webarticum.minibase.storage.api.TableIndex;
import hu.webarticum.minibase.storage.impl.diff.DiffTable;
import hu.webarticum.minibase.storage.impl.simple.SimpleColumnDefinition;
import hu.webarticum.minibase.storage.impl.simple.SimpleResourceManager;
import hu.webarticum.minibase.storage.impl.simple.SimpleSchema;
import hu.webarticum.minibase.storage.impl.simple.SimpleStorageAccess;
import hu.webarticum.miniconnect.record.converter.Converter;
import hu.webarticum.strex.Strex;

// TODO: split to builder and sub-builders
public class StorageAccessFactory {

    public static StorageAccess createStorageAccess(HoloConfig config, Converter converter) {
        SimpleStorageAccess storageAccess =  new SimpleStorageAccess();
        SimpleResourceManager<Schema> schemaManager = storageAccess.schemas();
        TreeRandom rootRandom = new HasherTreeRandom(config.seed());
        for (HoloConfigSchema schemaConfig : config.schemas()) {
            Schema schema = createSchema(config, schemaConfig, rootRandom, converter);
            schemaManager.register(schema);
        }
        return storageAccess;
    }

    private static Schema createSchema(
            HoloConfig config,
            HoloConfigSchema schemaConfig,
            TreeRandom rootRandom,
            Converter converter) {
        String schemaName = schemaConfig.name();
        TreeRandom schemaRandom = rootRandom.sub("schema-" + schemaName);
        SimpleSchema schema = new SimpleSchema(schemaName);
        SimpleResourceManager<Table> tableManager = schema.tables();
        for (HoloConfigTable tableConfig : schemaConfig.tables()) {
            Table table = createTable(config, schemaConfig, tableConfig, schemaRandom, converter);
            tableManager.register(table);
        }
        return schema;
    }
    
    private static Table createTable(
            HoloConfig config,
            HoloConfigSchema schemaConfig,
            HoloConfigTable tableConfig,
            TreeRandom schemaRandom,
            Converter converter) {
        LargeInteger tableSize = tableConfig.size();
        String tableName = tableConfig.name();
        TreeRandom tableRandom = schemaRandom.sub("table-" + tableName);
        ImmutableList<HoloConfigColumn> columnConfigs = tableConfig.columns();
        ImmutableList<String> columnNames = columnConfigs.map(HoloConfigColumn::name);
        ImmutableMap<String, Source<?>> columnSources = columnConfigs
                .assign(c -> createColumnSource(config, schemaConfig, tableConfig, c, tableRandom, converter))
                .map(HoloConfigColumn::name, s -> s);
        Optional<String> autoIncrementedName = extractAutoIncrementedColumnName(columnConfigs);
        ImmutableList<ColumnDefinition> columnDefinitions =
                columnConfigs.map(c -> createColumnDefinition(
                        c,
                        columnSources.get(c.name()),
                        autoIncrementedName.isPresent() && c.name().equals(autoIncrementedName.get())));
        NamedResourceStore<TableIndex> indexStore = createIndexStore(columnSources);
        boolean hasAutoIncremented = autoIncrementedName.isPresent();
        LargeInteger sequenceValue = hasAutoIncremented ? tableSize.increment() : LargeInteger.ONE;
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
    
    private static ColumnDefinition createColumnDefinition(
            HoloConfigColumn columnConfig, Source<?> source, boolean autoIncremented) {
        return new SimpleColumnDefinition(
                extractType(columnConfig, source),
                extractNullable(columnConfig, source),
                columnConfig.mode() == ColumnMode.COUNTER,
                autoIncremented,
                extractEnumValues(columnConfig, source),
                extractComparator(source),
                columnConfig.defaultValue());
    }

    private static Class<?> extractType(HoloConfigColumn columnConfig) {
        return extractType(columnConfig, null);
    }
    
    private static Class<?> extractType(HoloConfigColumn columnConfig, Source<?> source) {
        if (columnConfig.sourceFactory() != null && source != null) {
            return source.type();
        }
        
        if (columnConfig.mode() == ColumnMode.COUNTER || columnConfig.valuesForeignColumn() != null) {
            return LargeInteger.class;
        }
        
        Class<?> configType = columnConfig.type();
        if (configType != null) {
            return configType;
        }
        
        if (
                columnConfig.valuesBundle() != null ||
                columnConfig.valuesResource() != null ||
                columnConfig.valuesPattern() != null ||
                columnConfig.valuesDynamicPattern() != null) {
            return String.class;
        }
        
        if (columnConfig.valuesRange() != null) {
            return LargeInteger.class;
        }

        ImmutableList<Object> values = columnConfig.values();
        if (values != null && !values.isEmpty()) {
            return values.get(0).getClass();
        }
        
        if (source != null) {
            return source.type();
        }
        
        throw new IllegalArgumentException("Can not guess type for column: " + columnConfig.name());
    }
    
    private static boolean extractNullable(HoloConfigColumn columnConfig, Source<?> source) {
        if (columnConfig.sourceFactory() != null) {
            if (source instanceof IndexedSource) {
                return !((IndexedSource<?>) source).findNulls().isEmpty();
            } else {
                return true;
            }
        }
        
        return !columnConfig.nullCount().equals(LargeInteger.ZERO);
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
            HoloConfig config,
            HoloConfigSchema schemaConfig,
            HoloConfigTable tableConfig,
            HoloConfigColumn columnConfig,
            TreeRandom tableRandom,
            Converter converter) {
        LargeInteger tableSize = tableConfig.size();
        String columnName = columnConfig.name();
        
        Class<? extends SourceFactory> factoryClazz = columnConfig.sourceFactory();
        if (factoryClazz != null) {
            SourceFactory sourceFactory;
            try {
                sourceFactory = factoryClazz.newInstance();
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Factory instatiation failed", e);
            }
            return sourceFactory.create(
                    new ColumnLocation(schemaConfig.name(), tableConfig.name(), columnName),
                    tableRandom.sub("col-" + columnName),
                    tableSize,
                    columnConfig.sourceFactoryData());
        }
        
        ColumnMode columnMode = columnConfig.mode();
        if (columnMode == ColumnMode.DEFAULT || columnMode == ColumnMode.ENUM) {
            boolean isEnum = (columnMode == ColumnMode.ENUM);
            TreeRandom columnRandom = tableRandom.sub("col-" + columnName);
            if (columnConfig.valuesForeignColumn() != null) {
                return createForeignColumnSource(config, schemaConfig, tableConfig, columnConfig, columnRandom);
            } else if (columnConfig.valuesDynamicPattern() == null) {
                SortedSource<?> baseSource = loadBaseSource(columnConfig, converter, isEnum);
                return createShuffledSource(columnConfig, columnRandom, baseSource, tableSize);
            } else if (!isEnum) {
                return createDynamicPatternSource(columnConfig, columnRandom, converter, tableSize);
            } else {
                throw new IllegalArgumentException(
                        "ENUM mode can not be used with dynamic column (" +
                        tableConfig.name() + "." + columnName + ")");
            }
        } else if (columnMode == ColumnMode.COUNTER) {
            return new RangeSource(LargeInteger.ONE, tableSize);
        } else if (columnMode == ColumnMode.FIXED) {
            return createFixedSource(extractType(columnConfig), columnConfig.values().asList());
        } else {
            throw new IllegalArgumentException(
                    "Invalid column mode: " + columnMode + " (" + tableConfig.name() + "." + columnConfig.name() + ")");
        }
    }
    
    private static Source<?> createForeignColumnSource(
            HoloConfig config,
            HoloConfigSchema schemaConfig,
            HoloConfigTable tableConfig,
            HoloConfigColumn columnConfig,
            TreeRandom columnRandom) {
        ImmutableList<String> valuesForeignColumn = columnConfig.valuesForeignColumn();
        int size = valuesForeignColumn.size();
        if (size > 3) {
            throw new IllegalArgumentException("Too many items for valuesForeignColumn: " + valuesForeignColumn);
        }
        String foreignSchemaName = size == 3 ? valuesForeignColumn.get(0) : schemaConfig.name();
        String foreignTableName = size >= 2 ? valuesForeignColumn.get(size - 2) : tableConfig.name();
        String foreignColumnName = valuesForeignColumn.get(size - 1);
        HoloConfigSchema foreignSchemaConfig = config.schemas().stream()
                .filter(s -> s.name().equals(foreignSchemaName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Schema not found: " + foreignSchemaName));
        HoloConfigTable foreignTableConfig = foreignSchemaConfig.tables().stream()
                .filter(s -> s.name().equals(foreignTableName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Table not found: " + foreignTableName));
        HoloConfigColumn foreignColumnConfig = foreignTableConfig.columns().stream()
                .filter(s -> s.name().equals(foreignColumnName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Column not found: " + foreignTableName + "." + foreignTableName));
        
        if (foreignColumnConfig.mode() != ColumnMode.COUNTER) {
            throw new IllegalArgumentException();
        }
        
        LargeInteger foreignTableSize = foreignTableConfig.size();
        RangeSource rangeSource = new RangeSource(LargeInteger.ONE, foreignTableSize);
        LargeInteger tableSize = tableConfig.size();
        return createShuffledSource(columnConfig, columnRandom, rangeSource, tableSize);
    }

    private static SortedSource<?> loadBaseSource(HoloConfigColumn columnConfig, Converter converter, boolean isEnum) {
        if (columnConfig.valuesRange() != null) {
            return loadRangeSource(columnConfig, converter);
        } else if (columnConfig.valuesPattern() != null) {
            return loadPatternSource(columnConfig, converter);
        }
        
        List<Object> values = loadValues(columnConfig, converter);
        return createUniqueSource(extractType(columnConfig), values, isEnum);
    }

    private static SortedSource<?> loadRangeSource(HoloConfigColumn columnConfig, Converter converter) {
        ImmutableList<LargeInteger> valuesRange = columnConfig.valuesRange();
        Class<?> type = extractType(columnConfig);
        LargeInteger from = valuesRange.get(0);
        LargeInteger to = valuesRange.get(1);
        LargeInteger size = to.subtract(from).add(LargeInteger.ONE);
        RangeSource rangeSource = new RangeSource(from, size);
        if (type == LargeInteger.class) {
            return rangeSource;
        }
        
        if (type == LargeInteger.class) {
            return rangeSource;
        } else {
            return new TransformingSortedSource<LargeInteger, Object>( // NOSONAR explicit type parameters are necessary
                    rangeSource,
                    type,
                    v -> (LargeInteger) converter.convert(v, LargeInteger.class),
                    b -> converter.convert(b, type));
        }
    }

    private static SortedSource<?> loadPatternSource(HoloConfigColumn columnConfig, Converter converter) {
        StrexSource strexSource = new StrexSource(Strex.compile(columnConfig.valuesPattern()));
        Class<?> type = extractType(columnConfig);
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
        Class<?> columnClazz = extractType(columnConfig);
        ImmutableList<Object> rawValues = loadRawValues(columnConfig, converter);
        return rawValues.stream()
                .map(v -> converter.convert(v, columnClazz))
                .collect(Collectors.toList());
    }
    
    private static ImmutableList<Object> loadRawValues(HoloConfigColumn columnConfig, Converter converter) {
        String valuesResource = columnConfig.valuesResource();
        if (valuesResource != null) {
            return loadValuesFromResource(valuesResource);
        }
        
        String valuesBundle = columnConfig.valuesBundle();
        if (valuesBundle != null) {
            String bundleValuesResource = "hu/webarticum/holodb/values/" + valuesBundle + ".txt";
            return loadValuesFromResource(bundleValuesResource);
        }
        
        return columnConfig.values();
    }
    
    private static ImmutableList<Object> loadValuesFromResource(String resource) {
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
            return ImmutableList.fromCollection(values);
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
    
    private static <T> IndexedSource<T> createShuffledSource(
            HoloConfigColumn columnConfig, TreeRandom treeRandom, SortedSource<T> baseSource, LargeInteger tableSize) {
        LargeInteger nullCount = columnConfig.nullCount();
        LargeInteger valueCount = tableSize.subtract(nullCount);
        SortedSource<T> valueSource = new MonotonicSource<>(
                baseSource,
                new BinomialMonotonic(treeRandom.sub("monotonic"), valueCount, baseSource.size()));
        if (!valueCount.equals(tableSize)) {
            valueSource = new NullPaddedSortedSource<>(valueSource, tableSize);
        }
        Permutation permutation = createPermutation(treeRandom, columnConfig, tableSize);
        return new PermutatedIndexedSource<>(valueSource, permutation);
    }
    
    private static Source<?> createDynamicPatternSource(
            HoloConfigColumn columnConfig,
            TreeRandom columnRandom,
            Converter converter,
            LargeInteger tableSize) {
        LargeInteger nullCount = columnConfig.nullCount();
        LargeInteger valueCount = tableSize.subtract(nullCount);
        String dynamicPattern = columnConfig.valuesDynamicPattern();
        Class<?> type = extractType(columnConfig);
        GenerexSource generexSource = new GenerexSource(new Generex(dynamicPattern), columnRandom, valueCount);
        Source<?> source = generexSource;
        if (type != String.class) {
            source = new TransformingSource<String, Object>( // NOSONAR explicit type parameters are necessary
                    generexSource,
                    type,
                    b -> converter.convert(b, type));
        }
        if (!nullCount.equals(LargeInteger.ZERO)) {
            source = new NullPaddedSource<>(source, tableSize);
            Permutation permutation = createPermutation(columnRandom, columnConfig, tableSize);
            source = new PermutatedSource<>(source, permutation);
        }
        return source;
    }
    
    private static Permutation createPermutation(
            TreeRandom treeRandom, HoloConfigColumn columnConfig, LargeInteger tableSize) {
        ShuffleQuality shuffleQuality = columnConfig.shuffleQuality();
        if (shuffleQuality == null) {
            shuffleQuality = ShuffleQuality.MEDIUM;
        }
        
        return PermutationFactory.createPermutation(treeRandom.sub("permutation"), tableSize, shuffleQuality);
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
