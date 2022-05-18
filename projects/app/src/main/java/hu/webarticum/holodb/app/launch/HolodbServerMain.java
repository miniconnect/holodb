package hu.webarticum.holodb.app.launch;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import hu.webarticum.holodb.app.config.HoloConfig;
import hu.webarticum.holodb.app.config.HoloConfigColumn;
import hu.webarticum.holodb.app.config.HoloConfigColumn.ColumnMode;
import hu.webarticum.holodb.app.config.HoloConfigSchema;
import hu.webarticum.holodb.app.config.HoloConfigTable;
import hu.webarticum.holodb.core.data.binrel.monotonic.BinomialMonotonic;
import hu.webarticum.holodb.core.data.binrel.permutation.DirtyFpePermutation;
import hu.webarticum.holodb.core.data.binrel.permutation.Permutation;
import hu.webarticum.holodb.core.data.random.HasherTreeRandom;
import hu.webarticum.holodb.core.data.random.TreeRandom;
import hu.webarticum.holodb.core.data.source.UniqueSource;
import hu.webarticum.holodb.core.data.source.FixedSource;
import hu.webarticum.holodb.core.data.source.Index;
import hu.webarticum.holodb.core.data.source.IndexedSource;
import hu.webarticum.holodb.core.data.source.MonotonicSource;
import hu.webarticum.holodb.core.data.source.NullPaddedSource;
import hu.webarticum.holodb.core.data.source.PermutatedIndexedSource;
import hu.webarticum.holodb.core.data.source.RangeSource;
import hu.webarticum.holodb.core.data.source.SortedSource;
import hu.webarticum.holodb.core.data.source.Source;
import hu.webarticum.holodb.storage.GenericNamedResourceStore;
import hu.webarticum.holodb.storage.HoloSimpleSource;
import hu.webarticum.holodb.storage.HoloTable;
import hu.webarticum.holodb.storage.IndexTableIndex;
import hu.webarticum.miniconnect.api.MiniSessionManager;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;
import hu.webarticum.miniconnect.messenger.Messenger;
import hu.webarticum.miniconnect.messenger.impl.SessionManagerMessenger;
import hu.webarticum.miniconnect.rdmsframework.engine.Engine;
import hu.webarticum.miniconnect.rdmsframework.engine.impl.SimpleEngine;
import hu.webarticum.miniconnect.rdmsframework.execution.QueryExecutor;
import hu.webarticum.miniconnect.rdmsframework.execution.impl.IntegratedQueryExecutor;
import hu.webarticum.miniconnect.rdmsframework.parser.AntlrSqlParser;
import hu.webarticum.miniconnect.rdmsframework.parser.SqlParser;
import hu.webarticum.miniconnect.rdmsframework.session.FrameworkSessionManager;
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
import hu.webarticum.miniconnect.record.converter.DefaultConverter;
import hu.webarticum.miniconnect.server.MessengerServer;
import hu.webarticum.miniconnect.server.ServerConstants;

public class HolodbServerMain {
    
    private static final int SERVER_PORT = ServerConstants.DEFAULT_PORT;
    

    public static void main(String[] args) {
        HoloConfig config = loadConfig(args);
        SqlParser sqlParser = new AntlrSqlParser();
        QueryExecutor queryExecutor = new IntegratedQueryExecutor();
        Converter converter = new DefaultConverter();
        StorageAccess storageAccess = createStorageAccess(config, converter);
        try (Engine engine = new SimpleEngine(sqlParser, queryExecutor, storageAccess)) {
            MiniSessionManager sessionManager = new FrameworkSessionManager(engine);
            Messenger messenger = new SessionManagerMessenger(sessionManager);
            try (MessengerServer server = new MessengerServer(messenger, SERVER_PORT)) {
                System.out.println("Listen on " + SERVER_PORT);
                server.listen();
            }
        }
    }

    private static HoloConfig loadConfig(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("No config file was specified");
        }

        String configPath = args[0];
        File configFile = new File(configPath);
        if (!configFile.exists()) {
            throw new IllegalArgumentException("Config file not found: " + configPath);
        }
        
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            return mapper.readValue(configFile, HoloConfig.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static StorageAccess createStorageAccess(HoloConfig config, Converter converter) {
        SimpleStorageAccess storageAccess =  new SimpleStorageAccess();
        SimpleResourceManager<Schema> schemaManager = storageAccess.schemas();
        TreeRandom rootRandom = new HasherTreeRandom(config.seed());
        for (HoloConfigSchema schemaConfig : config.schemas()) {
            String schemaName = schemaConfig.name();
            TreeRandom schemaRandom = rootRandom.sub("schema-" + schemaName);
            SimpleSchema schema = new SimpleSchema(schemaName);
            schemaManager.register(schema);
            SimpleResourceManager<Table> tableManager = schema.tables();
            for (HoloConfigTable tableConfig : schemaConfig.tables()) {
                Table table = createTable(tableConfig, schemaRandom, converter);
                tableManager.register(table);
            }
        }
        return storageAccess;
    }
    
    private static Table createTable(
            HoloConfigTable tableConfig,
            TreeRandom schemaRandom,
            Converter converter) {
        BigInteger tableSize = tableConfig.size();
        String tableName = tableConfig.name();
        TreeRandom tableRandom = schemaRandom.sub("table-" + tableName);
        ImmutableList<HoloConfigColumn> columnConfigs =
                ImmutableList.fromCollection(tableConfig.columns());
        ImmutableList<String> columnNames = columnConfigs.map(HoloConfigColumn::name);
        ImmutableMap<String, Source<?>> columnSources = columnConfigs
                .assign(c -> createColumnSource(c, tableRandom, converter, tableSize))
                .map(HoloConfigColumn::name, s -> s);
        ImmutableList<ColumnDefinition> columnDefinitions =
                columnConfigs.map(c ->  new SimpleColumnDefinition(
                        c.type(),
                        !c.nullCount().equals(tableSize),
                        extractComparator(columnSources.get(c.name()))));
        NamedResourceStore<TableIndex> indexStore = createIndexStore(columnSources);
        Table table = new HoloTable(
                tableName,
                tableSize,
                columnNames,
                columnDefinitions,
                columnSources,
                ImmutableMap.empty(),
                indexStore);
        if (tableConfig.writeable()) {
            table = new DiffTable(table);
        }
        return table;
    }
    
    private static Source<?> createColumnSource(
            HoloConfigColumn columnConfig,
            TreeRandom tableRandom,
            Converter converter,
            BigInteger tableSize) {
        ColumnMode columnMode = columnConfig.mode();
        Class<?> columnClazz = columnConfig.type();
        List<Object> values = columnConfig.values().stream()
                .map(v -> converter.convert(v, columnClazz))
                .collect(Collectors.toList());
        if (columnMode == ColumnMode.DEFAULT) {
            UniqueSource<?> baseSource = createUniqueSource(columnClazz, values);
            TreeRandom columnRandom = tableRandom.sub("col-" + columnConfig.name());
            BigInteger nullCount = columnConfig.nullCount();
            return createDefaultSource(columnRandom, baseSource, tableSize, nullCount);
        } else if (columnMode == ColumnMode.COUNTER) {
            return new RangeSource(BigInteger.ONE, tableSize);
        } else if (columnMode == ColumnMode.FIXED) {
            return createFixedSource(columnConfig.type(), columnConfig.values());
        } else {
            throw new IllegalArgumentException("Invalid column mode: " + columnMode);
        }
    }

    private static Comparator<?> extractComparator(Source<?> source) {
        if (!(source instanceof Index)) {
            return null;
        }
        
        Index index = (Index) source;
        return index.comparator();
    }
    
    private static UniqueSource<?> createUniqueSource(Class<?> type, Collection<?> values) {
        try {
            return UniqueSource.class.getConstructor(Class.class, Collection.class)
                    .newInstance(type, values);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static <T> IndexedSource<T> createDefaultSource(
            TreeRandom treeRandom, SortedSource<T> baseSource, BigInteger tableSize, BigInteger nullCount) {
        BigInteger valueCount = tableSize.subtract(nullCount);
        SortedSource<T> valueSource = new MonotonicSource<>(
                baseSource,
                new BinomialMonotonic(treeRandom.sub("monotonic"), valueCount, baseSource.size()));
        if (!valueCount.equals(tableSize)) {
            valueSource = new NullPaddedSource<>(valueSource, tableSize);
        }
        Permutation permutation = new DirtyFpePermutation(treeRandom.sub("permutation"), tableSize);
        return new PermutatedIndexedSource<>(valueSource, permutation);
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
