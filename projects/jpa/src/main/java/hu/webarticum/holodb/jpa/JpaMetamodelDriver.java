package hu.webarticum.holodb.jpa;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import javax.persistence.metamodel.Metamodel;

import hu.webarticum.holodb.app.config.HoloConfig;
import hu.webarticum.holodb.app.factory.StorageAccessFactory;
import hu.webarticum.miniconnect.api.MiniSession;
import hu.webarticum.miniconnect.api.MiniSessionManager;
import hu.webarticum.miniconnect.jdbc.MiniJdbcConnection;
import hu.webarticum.miniconnect.jdbc.MiniJdbcDriver;
import hu.webarticum.miniconnect.jdbc.provider.DatabaseProvider;
import hu.webarticum.miniconnect.jdbc.provider.impl.BlanketDatabaseProvider;
import hu.webarticum.miniconnect.rdmsframework.engine.Engine;
import hu.webarticum.miniconnect.rdmsframework.engine.impl.LazyStorageEngine;
import hu.webarticum.miniconnect.rdmsframework.execution.QueryExecutor;
import hu.webarticum.miniconnect.rdmsframework.execution.impl.IntegratedQueryExecutor;
import hu.webarticum.miniconnect.rdmsframework.parser.AntlrSqlParser;
import hu.webarticum.miniconnect.rdmsframework.parser.SqlParser;
import hu.webarticum.miniconnect.rdmsframework.session.FrameworkSession;
import hu.webarticum.miniconnect.rdmsframework.session.FrameworkSessionManager;
import hu.webarticum.miniconnect.rdmsframework.storage.StorageAccess;
import hu.webarticum.miniconnect.record.converter.DefaultConverter;

public class JpaMetamodelDriver implements Driver {
    
    // FIXME
    public static Metamodel metamodel = null;
    
    
    public static synchronized void setMetamodel(Metamodel metamodel) {
        JpaMetamodelDriver.metamodel = metamodel;
    }

    public static synchronized Metamodel getMetamodel() {
        return metamodel;
    }
    

    @Override
    public boolean acceptsURL(String url) {
        
        // FIXME
        return url.startsWith("jdbc:holodb:jpa://");
        
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) {
        return new DriverPropertyInfo[0];
    }

    @Override
    public int getMajorVersion() {
        return MiniJdbcDriver.DRIVER_MAJOR_VERSION;
    }

    @Override
    public int getMinorVersion() {
        return MiniJdbcDriver.DRIVER_MINOR_VERSION;
    }

    @Override
    public boolean jdbcCompliant() {
        return true;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {

        // FIXME
        SqlParser sqlParser = new AntlrSqlParser();
        QueryExecutor queryExecutor = new IntegratedQueryExecutor();
        DatabaseProvider databaseProvider = new BlanketDatabaseProvider();
        AtomicReference<MiniSession> sessionHolder = new AtomicReference<>();
        Engine engine = new LazyStorageEngine(
                sqlParser,
                queryExecutor,
                () -> createMetamodelStorageAccess(),
                e -> ((FrameworkSession) sessionHolder.get()).engineSession().state().setCurrentSchema("economy"));
        MiniSessionManager sessionManager = new FrameworkSessionManager(engine);
        MiniSession session = sessionManager.openSession();
        sessionHolder.set(session);
        return new MiniJdbcConnection(session, databaseProvider);
        
    }
    
    private static StorageAccess createMetamodelStorageAccess() {
        
        // FIXME
        Metamodel metamodel = getMetamodel();
        if (metamodel == null) {
            throw new IllegalStateException("metamodel is null");
        }
        String selectedSchemaName = "economy";
        BigInteger seed = BigInteger.valueOf(42L);
        HoloConfig config = new JpaMetamodelHoloConfigLoader().load(metamodel, selectedSchemaName, seed);
        return StorageAccessFactory.createStorageAccess(config, new DefaultConverter());
        
    }
    
}
