package hu.webarticum.holodb.app.launch;

import java.math.BigInteger;

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
import hu.webarticum.miniconnect.rdmsframework.storage.StorageAccess;
import hu.webarticum.miniconnect.rdmsframework.storage.Table;
import hu.webarticum.miniconnect.rdmsframework.storage.impl.simple.SimpleColumnDefinition;
import hu.webarticum.miniconnect.rdmsframework.storage.impl.simple.SimpleStorageAccess;
import hu.webarticum.miniconnect.rdmsframework.storage.impl.simple.SimpleTableManager;
import hu.webarticum.miniconnect.server.MessengerServer;
import hu.webarticum.miniconnect.server.ServerConstants;

public class HolodbServerMain {
    
    private static final int SERVER_PORT = ServerConstants.DEFAULT_PORT;
    

    public static void main(String[] args) {
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

    public static StorageAccess createStorageAccess() {
        SimpleStorageAccess storageAccess =  new SimpleStorageAccess();
        SimpleTableManager tableManager = storageAccess.tables();
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
        tableManager.registerTable(table);
        return storageAccess;
    }
    
}
