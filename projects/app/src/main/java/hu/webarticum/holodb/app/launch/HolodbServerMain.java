package hu.webarticum.holodb.app.launch;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigInteger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import hu.webarticum.holodb.app.config.HoloConfig;
import hu.webarticum.holodb.storage.HoloTable;
import hu.webarticum.miniconnect.api.MiniSessionManager;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.messenger.Messenger;
import hu.webarticum.miniconnect.messenger.impl.SessionManagerMessenger;
import hu.webarticum.miniconnect.rdmsframework.engine.Engine;
import hu.webarticum.miniconnect.rdmsframework.engine.impl.SimpleEngine;
import hu.webarticum.miniconnect.rdmsframework.execution.QueryExecutor;
import hu.webarticum.miniconnect.rdmsframework.execution.SqlParser;
import hu.webarticum.miniconnect.rdmsframework.execution.simple.SimpleSelectExecutor;
import hu.webarticum.miniconnect.rdmsframework.query.AntlrSqlParser;
import hu.webarticum.miniconnect.rdmsframework.session.FrameworkSessionManager;
import hu.webarticum.miniconnect.rdmsframework.storage.Schema;
import hu.webarticum.miniconnect.rdmsframework.storage.StorageAccess;
import hu.webarticum.miniconnect.rdmsframework.storage.Table;
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
        QueryExecutor queryExecutor = new SimpleSelectExecutor();
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

    public static StorageAccess createStorageAccess() {
        SimpleStorageAccess storageAccess =  new SimpleStorageAccess();
        SimpleResourceManager<Schema> schemaManager = storageAccess.schemas();
        SimpleSchema schema = new SimpleSchema("default");
        schemaManager.register(schema);
        SimpleResourceManager<Table> tableManager = schema.tables();
        Table table = new HoloTable(
                "data",
                BigInteger.valueOf(100L),
                ImmutableList.of("id", "label", "description", "level"),
                ImmutableList.of(
                        new SimpleColumnDefinition(BigInteger.class, false),
                        new SimpleColumnDefinition(String.class, false),
                        new SimpleColumnDefinition(String.class, false),
                        new SimpleColumnDefinition(Integer.class, true)),
                null, // singleColumnSources
                null, // multiColumnSources
                null); // indexes
        tableManager.register(table);
        return storageAccess;
    }
    
}
