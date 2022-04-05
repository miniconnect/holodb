package hu.webarticum.holodb.app.launch;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import hu.webarticum.holodb.app.config.HoloConfig;
import hu.webarticum.holodb.app.config.HoloConfigColumn;
import hu.webarticum.holodb.app.config.HoloConfigColumn.ColumnMode;
import hu.webarticum.holodb.app.config.HoloConfigSchema;
import hu.webarticum.holodb.app.config.HoloConfigTable;
import hu.webarticum.holodb.core.data.random.HasherTreeRandom;
import hu.webarticum.holodb.core.data.random.TreeRandom;
import hu.webarticum.holodb.core.data.source.UniqueSource;
import hu.webarticum.holodb.core.data.source.FixedSource;
import hu.webarticum.holodb.core.data.source.RangeSource;
import hu.webarticum.holodb.core.data.source.SortedSource;
import hu.webarticum.holodb.core.data.source.Source;
import hu.webarticum.holodb.storage.GenericNamedResourceStore;
import hu.webarticum.holodb.storage.HoloSimpleSource;
import hu.webarticum.holodb.storage.HoloTable;
import hu.webarticum.miniconnect.api.MiniSessionManager;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;
import hu.webarticum.miniconnect.messenger.Messenger;
import hu.webarticum.miniconnect.messenger.impl.SessionManagerMessenger;
import hu.webarticum.miniconnect.rdmsframework.engine.Engine;
import hu.webarticum.miniconnect.rdmsframework.engine.impl.SimpleEngine;
import hu.webarticum.miniconnect.rdmsframework.execution.QueryExecutor;
import hu.webarticum.miniconnect.rdmsframework.execution.simple.SimpleQueryExecutor;
import hu.webarticum.miniconnect.rdmsframework.parser.AntlrSqlParser;
import hu.webarticum.miniconnect.rdmsframework.parser.SqlParser;
import hu.webarticum.miniconnect.rdmsframework.session.FrameworkSessionManager;
import hu.webarticum.miniconnect.rdmsframework.storage.ColumnDefinition;
import hu.webarticum.miniconnect.rdmsframework.storage.NamedResourceStore;
import hu.webarticum.miniconnect.rdmsframework.storage.Schema;
import hu.webarticum.miniconnect.rdmsframework.storage.StorageAccess;
import hu.webarticum.miniconnect.rdmsframework.storage.Table;
import hu.webarticum.miniconnect.rdmsframework.storage.TableIndex;
import hu.webarticum.miniconnect.rdmsframework.storage.impl.simple.SimpleColumnDefinition;
import hu.webarticum.miniconnect.rdmsframework.storage.impl.simple.SimpleResourceManager;
import hu.webarticum.miniconnect.rdmsframework.storage.impl.simple.SimpleSchema;
import hu.webarticum.miniconnect.rdmsframework.storage.impl.simple.SimpleStorageAccess;
import hu.webarticum.miniconnect.server.MessengerServer;
import hu.webarticum.miniconnect.server.ServerConstants;

public class HolodbServerMain {
    
    private static final int SERVER_PORT = ServerConstants.DEFAULT_PORT;
    

    public static void main(String[] args) {
        HoloConfig config = loadConfig(args);
        SqlParser sqlParser = new AntlrSqlParser();
        QueryExecutor queryExecutor = new SimpleQueryExecutor();
        StorageAccess storageAccess = createStorageAccess(config);
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

    public static StorageAccess createStorageAccess(HoloConfig config) {
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
                Table table = createTable(schemaRandom, tableConfig);
                tableManager.register(table);
            }
        }
        return storageAccess;
    }
    
    private static Table createTable(TreeRandom schemaRandom, HoloConfigTable tableConfig) {
        BigInteger tableSize = tableConfig.size();
        String tableName = tableConfig.name();
        TreeRandom tableRandom = schemaRandom.sub("table-" + tableName);
        ImmutableList<HoloConfigColumn> columnConfigs =
                ImmutableList.fromCollection(tableConfig.columns());
        ImmutableList<String> columnNames = columnConfigs.map(HoloConfigColumn::name);
        ImmutableList<ColumnDefinition> columnDefinitions =
                columnConfigs.map(c -> new SimpleColumnDefinition(c.type(), false));
        ImmutableMap<String, Source<?>> columnSources = columnConfigs
                .assign(c -> createColumnSource(tableRandom, tableSize, c))
                .map(HoloConfigColumn::name, s -> s);
        NamedResourceStore<TableIndex> indexStore = createIndexStore(columnSources);
        return new HoloTable(
                tableName,
                tableSize,
                columnNames,
                columnDefinitions,
                columnSources,
                ImmutableMap.empty(),
                indexStore);
    }
    
    private static Source<?> createColumnSource(
            TreeRandom tableRandom, BigInteger tableSize, HoloConfigColumn columnConfig) {
        ColumnMode columnMode = columnConfig.mode();
        if (columnMode == ColumnMode.DEFAULT) {
            SortedSource<?> baseSource =
                    createUniqueSource(columnConfig.type(), columnConfig.values());
            TreeRandom columnRandom = tableRandom.sub("col-" + columnConfig.name());
            return new HoloSimpleSource<>(columnRandom, baseSource, tableSize);
        } else if (columnMode == ColumnMode.COUNTER) {
            return new RangeSource(BigInteger.ONE, tableSize);
        } else if (columnMode == ColumnMode.FIXED) {
            return createFixedSource(columnConfig.type(), columnConfig.values());
        } else {
            throw new IllegalArgumentException("Invalid column mode: " + columnMode);
        }
    }

    private static UniqueSource<?> createUniqueSource(Class<?> type, Collection<?> values) {
        try {
            return UniqueSource.class.getConstructor(Class.class, Collection.class)
                    .newInstance(type, values);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static FixedSource<?> createFixedSource(Class<?> type, Collection<?> values) {
        try {
            return FixedSource.class.getConstructor(Class.class, Collection.class)
                    .newInstance(type, values);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    // FIXME: index for COUNTER/RangeSource etc. too
    private static NamedResourceStore<TableIndex> createIndexStore(
            ImmutableMap<String, Source<?>> columnSources) {
        List<TableIndex> tableIndexes = new ArrayList<>();
        for (Map.Entry<String, Source<?>> entry : columnSources.entrySet()) {
            Source<?> source = entry.getValue();
            if (source instanceof HoloSimpleSource) {
                String columnName = entry.getKey();
                TableIndex tableIndex = ((HoloSimpleSource<?>) source)
                        .createIndex("idx_" + columnName, columnName);
                tableIndexes.add(tableIndex);
            }
        }
        return GenericNamedResourceStore.from(tableIndexes);
    }

}
