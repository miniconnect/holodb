package hu.webarticum.holodb.app.launch;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.webarticum.holodb.bootstrap.factory.ConfigLoader;
import hu.webarticum.holodb.bootstrap.factory.EngineBuilder;
import hu.webarticum.holodb.bootstrap.factory.StorageAccessFactory;
import hu.webarticum.holodb.config.HoloConfig;
import hu.webarticum.minibase.engine.api.Engine;
import hu.webarticum.minibase.engine.facade.FrameworkSessionManager;
import hu.webarticum.minibase.storage.api.StorageAccess;
import hu.webarticum.miniconnect.api.MiniSessionManager;
import hu.webarticum.miniconnect.messenger.Messenger;
import hu.webarticum.miniconnect.messenger.impl.SessionManagerMessenger;
import hu.webarticum.miniconnect.server.MessengerServer;
import hu.webarticum.miniconnect.server.ServerConstants;

@Command(name = "HoloDBServer", mixinStandardHelpOptions = true) 
public class HolodbServerMain implements Runnable { 

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    private static final int SERVER_PORT = ServerConstants.DEFAULT_PORT;

    @Option(names = "--watch", description = "Enables watching changes of the configuration file.") 
    private boolean watch = false;
    
    private Thread watchThread = null;

    @Parameters(
            paramLabel = "<config-file-path>",
            description = "Path to the configuration file.",
            arity = "1")
    private String configFilePath = null;

    @Override
    public void run() {
        try (Engine engine = loadEngine(configFilePath)) {
            try (MessengerServer server = createServer(engine, SERVER_PORT)) {
                logger.info("Listen on {}", SERVER_PORT);
                server.listen();
            }
        }
        if (watchThread != null) {
            watchThread.interrupt();
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new HolodbServerMain()).execute(args); 
        System.exit(exitCode); 
    }

    private Engine loadEngine(String configFilePath) {
        if (watch) {
            return loadDynamicEngine(configFilePath);
        } else {
            return loadStaticEngine(configFilePath);
        }
    }
    
    private Engine loadStaticEngine(String configFilePath) {
        HoloConfig config = loadConfig(configFilePath);
        return EngineBuilder.ofConfig(config).build();
    }

    private Engine loadDynamicEngine(String configFilePath) {
        StorageAccess[] storageAccessRef = new StorageAccess[] { loadStorageAccess(configFilePath) };
        startWatchingConfigFile(configFilePath, () -> storageAccessRef[0] = loadStorageAccess(configFilePath));
        return EngineBuilder.ofStorageAccessSupplier(() -> storageAccessRef[0]).build();
    }

    private void startWatchingConfigFile(String configFilePath, Runnable consumer) {
        watchThread = new Thread(() -> watchConfigFile(configFilePath, consumer));
        watchThread.start();
    }
    
    private void watchConfigFile(String configFilePath, Runnable callback) {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            Path filePath = Paths.get(configFilePath);
            Path dirPath = filePath.getParent();
            dirPath.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);
            logger.info("Watching file: {} ...", filePath.getFileName());
            while (true) {
                WatchKey key = watchService.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    Path changedPath = (Path) event.context();
                    if (changedPath.equals(filePath.getFileName())) {
                        logger.info("File {} has changed ({})", changedPath, kind.name());
                        callback.run();
                    }
                }
                if (!key.reset()) {
                    logger.info("WatchKey is no longer valid");
                    break;
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private StorageAccess loadStorageAccess(String configFilePath) {
        HoloConfig config = loadConfig(configFilePath);
        return StorageAccessFactory.createStorageAccess(config);
    }
    
    private HoloConfig loadConfig(String configFilePath) {
        File configFile = new File(configFilePath);
        return new ConfigLoader(configFile).load();
    }
    
    private MessengerServer createServer(Engine engine, int port) {
        MiniSessionManager sessionManager = new FrameworkSessionManager(engine);
        Messenger messenger = new SessionManagerMessenger(sessionManager);
        return new MessengerServer(messenger, port);
    }

}