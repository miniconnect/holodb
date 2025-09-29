package hu.webarticum.holodb.jpa;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import hu.webarticum.holodb.bootstrap.factory.StorageAccessFactory;
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
import jakarta.persistence.metamodel.Metamodel;

public class JpaMetamodelDriver implements Driver {
    
    public static final String URL_PREFIX = "jdbc:holodb:jpa://";
    
    public static final String DEFAULT_SCHEMA_NAME = "default_schema";
    
    
    private static final String JAKARTA_METAMODEL_TYPE_NAME = "jakarta.persistence.metamodel.Metamodel";
    
    private static final String JAVAX_METAMODEL_TYPE_NAME = "javax.persistence.metamodel.Metamodel";
    
    
    private static Object metamodel = null;
    
    /**
     * Accepts a metamodel.
     * The currently supported types are the following:
     * 
     * <ul>
     *   <li>{@link jakarta.persistence.metamodel.Metamodel}</li>
     *   <li>{@link javax.persistence.metamodel.Metamodel}</li>
     * </ul>
     */
    public static synchronized void setMetamodel(Object metamodel) {
        JpaMetamodelDriver.metamodel = metamodel;
    }

    private static synchronized Object getMetamodel() {
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
        LargeInteger seed = LargeInteger.of(42L); // FIXME: detect?
        HoloConfig config;
        Object metamodel = getMetamodel();
        if (isInstanceOf(metamodel, JAKARTA_METAMODEL_TYPE_NAME)) {
            Metamodel jakartaMetamodel = (Metamodel) metamodel;
            config = new JpaJakartaMetamodelHoloConfigLoader().load(jakartaMetamodel, defaultSchemaName, seed);
        } else if (isInstanceOf(metamodel, JAVAX_METAMODEL_TYPE_NAME)) {
            javax.persistence.metamodel.Metamodel javaxMetamodel = (javax.persistence.metamodel.Metamodel) metamodel;
            config = new JpaJavaxMetamodelHoloConfigLoader().load(javaxMetamodel, defaultSchemaName, seed);
        } else {
            throw new StorageAccessNotReadyException();
        }
        return StorageAccessFactory.createStorageAccess(config, new DefaultConverter());
    }
    
    private static boolean isInstanceOf(Object object, String type) {
        Class<?> clazz;
        try {
            clazz = Class.forName(type);
        } catch (ClassNotFoundException e) {
            return false;
        }
        
        return clazz.isInstance(object);
    }

    private static void setupDefaultSchema(AtomicReference<MiniSession> sessionHolder, String defaultSchemaName) {
        ((FrameworkSession) sessionHolder.get()).engineSession().state().setCurrentSchema(defaultSchemaName);
    }
    
}
