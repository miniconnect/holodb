package hu.webarticum.holodb.app.factory;

import java.util.Objects;

import hu.webarticum.holodb.app.config.HoloConfig;
import hu.webarticum.miniconnect.rdmsframework.engine.Engine;
import hu.webarticum.miniconnect.rdmsframework.engine.impl.SimpleEngine;
import hu.webarticum.miniconnect.rdmsframework.execution.QueryExecutor;
import hu.webarticum.miniconnect.rdmsframework.execution.impl.IntegratedQueryExecutor;
import hu.webarticum.miniconnect.rdmsframework.parser.AntlrSqlParser;
import hu.webarticum.miniconnect.rdmsframework.parser.SqlParser;
import hu.webarticum.miniconnect.rdmsframework.storage.StorageAccess;
import hu.webarticum.miniconnect.record.converter.Converter;
import hu.webarticum.miniconnect.record.converter.DefaultConverter;

public class EngineBuilder {
    
    private final HoloConfig config;
    
    private final Converter converter;
    
    private final StorageAccess storageAccess;

    private SqlParser sqlParser = null;
    
    private QueryExecutor queryExecutor = null;
    
    
    private EngineBuilder(HoloConfig config, Converter converter, StorageAccess storageAccess) {
        this.config = config;
        this.converter = converter;
        this.storageAccess = storageAccess;
    }
    
    
    public static EngineBuilder ofConfig(HoloConfig config) {
        return new EngineBuilder(config, null, null);
    }

    public static EngineBuilder ofConfig(HoloConfig config, Converter converter) {
        return new EngineBuilder(config, converter, null);
    }

    public static EngineBuilder ofStorageAccess(StorageAccess storageAccess) {
        return new EngineBuilder(null, null, storageAccess);
    }
    
    
    public void sqlParser(SqlParser sqlParser) {
        this.sqlParser = sqlParser;
    }

    public void queryExecutor(QueryExecutor queryExecutor) {
        this.queryExecutor = queryExecutor;
    }
    
    
    public Engine build() {
        SqlParser sqlParserToInject = Objects.requireNonNullElseGet(
                sqlParser, this::buildDefaultSqlParser);
        QueryExecutor queryExecutorToInject = Objects.requireNonNullElseGet(
                queryExecutor, this::buildDefaultQueryExecutor);
        StorageAccess storageAccessToInject = Objects.requireNonNullElseGet(
                storageAccess, this::buildStorageAccessFromConfig);
        return new SimpleEngine(sqlParserToInject, queryExecutorToInject, storageAccessToInject);
    }

    private SqlParser buildDefaultSqlParser() {
        return new AntlrSqlParser();
    }

    private QueryExecutor buildDefaultQueryExecutor() {
        return new IntegratedQueryExecutor();
    }

    private StorageAccess buildStorageAccessFromConfig() {
        Converter converterToInject = Objects.requireNonNullElseGet(converter, this:: buildDefaultConverter);
        return StorageAccessFactory.createStorageAccess(config, converterToInject);
    }

    private Converter buildDefaultConverter() {
        return new DefaultConverter();
    }

}
