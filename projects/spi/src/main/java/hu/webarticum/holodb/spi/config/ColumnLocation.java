package hu.webarticum.holodb.spi.config;

public class ColumnLocation {
    
    private final String schemaName;
    
    private final String tableName;
    
    private final String columnName;
    
    
    public ColumnLocation(String schemaName, String tableName, String columnName) {
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.columnName = columnName;
    }
    

    public String schemaName() {
        return schemaName;
    }

    public String tableName() {
        return tableName;
    }

    public String columnName() {
        return columnName;
    }

}
