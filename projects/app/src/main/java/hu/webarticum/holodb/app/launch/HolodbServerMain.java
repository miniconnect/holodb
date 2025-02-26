package hu.webarticum.holodb.app.launch;

import java.io.File;
import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.webarticum.holodb.bootstrap.factory.ConfigLoader;
import hu.webarticum.holodb.bootstrap.factory.EngineBuilder;
import hu.webarticum.holodb.config.HoloConfig;
import hu.webarticum.minibase.engine.api.Engine;
import hu.webarticum.minibase.engine.facade.FrameworkSessionManager;
import hu.webarticum.miniconnect.api.MiniSessionManager;
import hu.webarticum.miniconnect.messenger.Messenger;
import hu.webarticum.miniconnect.messenger.impl.SessionManagerMessenger;
import hu.webarticum.miniconnect.server.MessengerServer;
import hu.webarticum.miniconnect.server.ServerConstants;

public class HolodbServerMain {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    
    private static final int SERVER_PORT = ServerConstants.DEFAULT_PORT;
    

    public static void main(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("No config file was specified");
        }
        
        String configFilePath = args[0];
        try (Engine engine = createEngine(configFilePath)) {
            try (MessengerServer server = createServer(engine, SERVER_PORT)) {
                logger.info("Listen on {}", SERVER_PORT);
                server.listen();
            }
        }
    }
    
    
    private static Engine createEngine(String configFilePath) {
        File configFile = new File(configFilePath);
        HoloConfig config = new ConfigLoader(configFile).load();
        return EngineBuilder.ofConfig(config).build();
    }
    
    private static MessengerServer createServer(Engine engine, int port) {
        MiniSessionManager sessionManager = new FrameworkSessionManager(engine);
        Messenger messenger = new SessionManagerMessenger(sessionManager);
        return new MessengerServer(messenger, port);
    }

}
