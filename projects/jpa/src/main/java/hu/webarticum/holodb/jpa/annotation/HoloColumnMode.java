package hu.webarticum.holodb.jpa.annotation;

import hu.webarticum.holodb.app.config.HoloConfigColumn.ColumnMode;

public enum HoloColumnMode {

    DEFAULT(ColumnMode.DEFAULT),
    
    COUNTER(ColumnMode.COUNTER),
    
    FIXED(ColumnMode.FIXED),
    
    ENUM(ColumnMode.ENUM),
    
    UNDEFINED(null),
    
    ;
    
    
    private final ColumnMode columnMode;
    
    
    private HoloColumnMode(ColumnMode columnMode) {
        this.columnMode = columnMode;
    }
    
    
    public ColumnMode columnMode() {
        return columnMode;
    }
    
}
