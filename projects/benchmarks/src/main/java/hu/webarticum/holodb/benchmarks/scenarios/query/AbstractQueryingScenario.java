package hu.webarticum.holodb.benchmarks.scenarios.query;

import hu.webarticum.holodb.benchmarks.framework.Scenario;
import hu.webarticum.holodb.bootstrap.factory.ConfigLoader;
import hu.webarticum.holodb.bootstrap.factory.EngineBuilder;
import hu.webarticum.holodb.config.HoloConfig;
import hu.webarticum.minibase.engine.api.Engine;
import hu.webarticum.minibase.engine.facade.FrameworkSessionManager;
import hu.webarticum.miniconnect.api.MiniErrorException;
import hu.webarticum.miniconnect.api.MiniResult;
import hu.webarticum.miniconnect.api.MiniSession;
import hu.webarticum.miniconnect.api.MiniSessionManager;

public abstract class AbstractQueryingScenario implements Scenario {

    private static final String CONFIG_RESOURCE_PATH = "hu/webarticum/holodb/benchmarks/scenarios/query/config.yaml";
    
    private MiniSession session;
    
    @Override
    public void setUp() {
        HoloConfig config = new ConfigLoader(CONFIG_RESOURCE_PATH).load();
        Engine engine = EngineBuilder.ofConfig(config).build();
        MiniSessionManager sessionManager = new FrameworkSessionManager(engine);
        session = sessionManager.openSession();
        executeOrThrow(session, "USE benchmark");
    }
    
    @Override
    public void runOnce() {
        runWithSession(session);
    }

    @Override
    public void tearDown() {
        if (session != null) {
            session.close();
        }
    }
    
    protected MiniResult executeOrThrow(MiniSession session, String sql) {
        MiniResult result = session.execute(sql);
        if (!result.success()) {
            throw new MiniErrorException(result.error());
        }
        return result;
    }

    protected abstract void runWithSession(MiniSession session);

}
