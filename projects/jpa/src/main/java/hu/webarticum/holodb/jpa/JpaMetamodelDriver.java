package hu.webarticum.holodb.jpa;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import javax.persistence.metamodel.Metamodel;

import hu.webarticum.holodb.app.factory.StorageAccessFactory;
import hu.webarticum.holodb.config.HoloConfig;
import hu.webarticum.minibase.engine.api.Engine;
import hu.webarticum.minibase.engine.facade.FrameworkSession;
import hu.webarticum.minibase.engine.facade.FrameworkSessionManager;
import hu.webarticum.minibase.engine.impl.LazyStorageEngine;
import hu.webarticum.minibase.engine.impl.LazyStorageEngine.StorageAccessNotReadyException;
import hu.webarticum.minibase.execution.QueryExecutor;
import hu.webarticum.minibase.execution.impl.IntegratedQueryExecutor;
import hu.webarticum.minibase.query.parser.AntlrSqlParser;
import hu.webarticum.minibase.query.parser.SqlParser;
import hu.webarticum.minibase.storage.api.StorageAccess;
import hu.webarticum.miniconnect.api.MiniSession;
import hu.webarticum.miniconnect.api.MiniSessionManager;
import hu.webarticum.miniconnect.jdbc.MiniJdbcConnection;
import hu.webarticum.miniconnect.jdbc.MiniJdbcDriver;
import hu.webarticum.miniconnect.jdbc.provider.DatabaseProvider;
import hu.webarticum.miniconnect.jdbc.provider.impl.BlanketDatabaseProvider;
import hu.webarticum.miniconnect.lang.LargeInteger;
import hu.webarticum.miniconnect.record.converter.DefaultConverter;

public class JpaMetamodelDriver implements Driver {
    
    public static final String URL_PREFIX = "jdbc:holodb:jpa://";
    
    public static final String DEFAULT_SCHEMA_NAME = "default_schema";
    
    
    private static Metamodel metamodel = null;
    
    public static synchronized void setMetamodel(Metamodel metamodel) {
        JpaMetamodelDriver.metamodel = metamodel;
    }

    private static synchronized Metamodel getMetamodel() {
        return metamodel;
    }
    

    @Override
    public boolean acceptsURL(String url) {
        return url.startsWith(URL_PREFIX);
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
        int prefixLength = URL_PREFIX.length();
        String defaultSchemaName =
                (url.length() > prefixLength) ?
                url.substring(prefixLength + 1) :
                DEFAULT_SCHEMA_NAME;
        SqlParser sqlParser = new AntlrSqlParser();
        QueryExecutor queryExecutor = new IntegratedQueryExecutor();
        DatabaseProvider databaseProvider = new BlanketDatabaseProvider();
        AtomicReference<MiniSession> sessionHolder = new AtomicReference<>();
        Engine engine = new LazyStorageEngine(
                sqlParser,
                queryExecutor,
                () -> createMetamodelStorageAccess(defaultSchemaName),
                e -> setupDefaultSchema(sessionHolder, defaultSchemaName));
        MiniSessionManager sessionManager = new FrameworkSessionManager(engine);
        MiniSession session = sessionManager.openSession();
        sessionHolder.set(session);
        return new MiniJdbcConnection(session, databaseProvider);
    }
    
    private static StorageAccess createMetamodelStorageAccess(String defaultSchemaName) {
        Metamodel metamodel = getMetamodel();
        if (metamodel == null) {
            throw new StorageAccessNotReadyException();
        }
        LargeInteger seed = LargeInteger.of(42L); // FIXME: detect?
        HoloConfig config = new JpaMetamodelHoloConfigLoader().load(metamodel, defaultSchemaName, seed);
        return StorageAccessFactory.createStorageAccess(config, new DefaultConverter());
    }
    
    private static void setupDefaultSchema(AtomicReference<MiniSession> sessionHolder, String defaultSchemaName) {
        ((FrameworkSession) sessionHolder.get()).engineSession().state().setCurrentSchema(defaultSchemaName);
    }
    
}
