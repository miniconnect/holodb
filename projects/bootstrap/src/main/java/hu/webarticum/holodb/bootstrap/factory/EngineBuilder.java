package hu.webarticum.holodb.bootstrap.factory;

import java.util.function.Supplier;

import hu.webarticum.holodb.config.HoloConfig;
import hu.webarticum.minibase.engine.api.Engine;
import hu.webarticum.minibase.engine.impl.DynamicStorageEngine;
import hu.webarticum.minibase.engine.impl.SimpleEngine;
import hu.webarticum.minibase.execution.QueryExecutor;
import hu.webarticum.minibase.execution.impl.IntegratedQueryExecutor;
import hu.webarticum.minibase.query.parser.AntlrSqlParser;
import hu.webarticum.minibase.query.parser.SqlParser;
import hu.webarticum.minibase.storage.api.StorageAccess;
import hu.webarticum.miniconnect.record.converter.Converter;
import hu.webarticum.miniconnect.record.converter.DefaultConverter;

public class EngineBuilder {
    
    private final HoloConfig config;
    
    private final Converter converter;
    
    private final StorageAccess storageAccess;
    
    private final Supplier<StorageAccess> storageAccessSupplier;

    private SqlParser sqlParser = null;
    
    private QueryExecutor queryExecutor = null;
    
    
    private EngineBuilder(
            HoloConfig config,
            Converter converter,
            StorageAccess storageAccess,
            Supplier<StorageAccess> storageAccessSupplier) {
        this.config = config;
        this.converter = converter;
        this.storageAccess = storageAccess;
        this.storageAccessSupplier = storageAccessSupplier;
    }
    
    
    public static EngineBuilder ofConfig(HoloConfig config) {
        return new EngineBuilder(config, null, null, null);
    }

    public static EngineBuilder ofConfig(HoloConfig config, Converter converter) {
        return new EngineBuilder(config, converter, null, null);
    }

    public static EngineBuilder ofStorageAccess(StorageAccess storageAccess) {
        return new EngineBuilder(null, null, storageAccess, null);
    }
    
    public static EngineBuilder ofStorageAccessSupplier(Supplier<StorageAccess> storageAccessSupplier) {
        return new EngineBuilder(null, null, null, storageAccessSupplier);
    }
    
    
    public EngineBuilder sqlParser(SqlParser sqlParser) {
        this.sqlParser = sqlParser;
        return this;
    }

    public EngineBuilder queryExecutor(QueryExecutor queryExecutor) {
        this.queryExecutor = queryExecutor;
        return this;
    }
    
    
    public Engine build() {
        SqlParser sqlParserToInject = sqlParser != null ? sqlParser : buildDefaultSqlParser();
        QueryExecutor queryExecutorToInject = queryExecutor != null ? queryExecutor : buildDefaultQueryExecutor();
        if (storageAccessSupplier != null) {
            return new DynamicStorageEngine(sqlParserToInject, queryExecutorToInject, storageAccessSupplier);
        } else {
            StorageAccess storageAccessToInject = storageAccess != null ? storageAccess : buildStorageAccessFromConfig();
            return new SimpleEngine(sqlParserToInject, queryExecutorToInject, storageAccessToInject);
        }
    }

    private SqlParser buildDefaultSqlParser() {
        return new AntlrSqlParser();
    }

    private QueryExecutor buildDefaultQueryExecutor() {
        return new IntegratedQueryExecutor();
    }

    private StorageAccess buildStorageAccessFromConfig() {
        Converter converterToInject = converter != null ? converter : buildDefaultConverter();
        return StorageAccessFactory.createStorageAccess(config, converterToInject);
    }

    private Converter buildDefaultConverter() {
        return new DefaultConverter();
    }

}
