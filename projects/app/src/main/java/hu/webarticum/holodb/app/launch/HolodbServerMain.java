package hu.webarticum.holodb.app.launch;

import java.io.File;

import hu.webarticum.holodb.app.config.HoloConfig;
import hu.webarticum.holodb.app.factory.ConfigLoader;
import hu.webarticum.holodb.app.factory.EngineBuilder;
import hu.webarticum.miniconnect.api.MiniSessionManager;
import hu.webarticum.miniconnect.messenger.Messenger;
import hu.webarticum.miniconnect.messenger.impl.SessionManagerMessenger;
import hu.webarticum.miniconnect.rdmsframework.engine.Engine;
import hu.webarticum.miniconnect.rdmsframework.session.FrameworkSessionManager;
import hu.webarticum.miniconnect.server.MessengerServer;
import hu.webarticum.miniconnect.server.ServerConstants;

public class HolodbServerMain {
    
    private static final int SERVER_PORT = ServerConstants.DEFAULT_PORT;
    

    public static void main(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("No config file was specified");
        }
        
        String configFilePath = args[0];
        try (Engine engine = createEngine(configFilePath)) {
            try (MessengerServer server = createServer(engine, SERVER_PORT)) {
                System.out.println("Listen on " + SERVER_PORT);
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
