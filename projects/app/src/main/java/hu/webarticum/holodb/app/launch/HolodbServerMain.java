package hu.webarticum.holodb.app.launch;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigInteger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import hu.webarticum.holodb.app.config.HoloConfig;
import hu.webarticum.holodb.core.data.random.HasherTreeRandom;
import hu.webarticum.holodb.core.data.random.TreeRandom;
import hu.webarticum.holodb.core.data.source.UniqueSource;
import hu.webarticum.holodb.core.data.source.RangeSource;
import hu.webarticum.holodb.core.data.source.Source;
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
import hu.webarticum.miniconnect.rdmsframework.execution.SqlParser;
import hu.webarticum.miniconnect.rdmsframework.execution.simple.SimpleQueryExecutor;
import hu.webarticum.miniconnect.rdmsframework.query.AntlrSqlParser;
import hu.webarticum.miniconnect.rdmsframework.session.FrameworkSessionManager;
import hu.webarticum.miniconnect.rdmsframework.storage.Schema;
import hu.webarticum.miniconnect.rdmsframework.storage.StorageAccess;
import hu.webarticum.miniconnect.rdmsframework.storage.Table;
import hu.webarticum.miniconnect.rdmsframework.storage.impl.simple.EmptyNamedResourceStore;
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
        StorageAccess storageAccess = createStorageAccess();
        try (Engine engine = new SimpleEngine(sqlParser, queryExecutor, storageAccess)) {
            MiniSessionManager sessionManager = new FrameworkSessionManager(engine);
            Messenger messenger = new SessionManagerMessenger(sessionManager);
            try (MessengerServer server = new MessengerServer(messenger, SERVER_PORT)) {
                
                System.out.println("Listen on " + SERVER_PORT);
                // FIXME
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

    // FIXME
    public static StorageAccess createStorageAccess() {
        SimpleStorageAccess storageAccess =  new SimpleStorageAccess();
        SimpleResourceManager<Schema> schemaManager = storageAccess.schemas();
        SimpleSchema schema = new SimpleSchema("default");
        schemaManager.register(schema);
        SimpleResourceManager<Table> tableManager = schema.tables();
        BigInteger size = BigInteger.valueOf(100);
        TreeRandom rootRandom = new HasherTreeRandom("some-key-23874328");

        Source<String> labelSource = new HoloSimpleSource<>(
                rootRandom.sub("col-id"),
                new UniqueSource<>("First", "Second", "Third"),
                size);
        Source<String> descriptionSource = new HoloSimpleSource<>(
                rootRandom.sub("col-description"),
                new UniqueSource<>("Lorem", "Ipsum", "Dolor"),
                size);
        Source<Integer> levelSource = new HoloSimpleSource<>(
                rootRandom.sub("col-level"),
                new UniqueSource<>(1, 2, 3, 4, 5),
                size);
        
        Table table = new HoloTable(
                "data",
                size,
                ImmutableList.of("id", "label", "description", "level"),
                ImmutableList.of(
                        new SimpleColumnDefinition(BigInteger.class, false),
                        new SimpleColumnDefinition(String.class, false),
                        new SimpleColumnDefinition(String.class, false),
                        new SimpleColumnDefinition(Integer.class, true)),
                ImmutableMap.of(
                        "id", new RangeSource(BigInteger.ONE, size),
                        "label", labelSource,
                        "description", descriptionSource,
                        "level", levelSource),
                ImmutableMap.empty(),
                new EmptyNamedResourceStore<>());
        
        tableManager.register(table);
        return storageAccess;
    }
    
}
