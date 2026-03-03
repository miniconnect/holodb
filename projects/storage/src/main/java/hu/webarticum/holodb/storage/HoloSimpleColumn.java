package hu.webarticum.holodb.storage;

import java.util.Optional;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.minibase.storage.api.Column;
import hu.webarticum.minibase.storage.api.ColumnDefinition;

public class HoloSimpleColumn implements Column {

    private final String name;

    private final ColumnDefinition definition;

    private final ImmutableList<Object> possibleValues;


    public HoloSimpleColumn(String name, ColumnDefinition definition) {
        this(name, definition, null);
    }

    public HoloSimpleColumn(String name, ColumnDefinition definition, ImmutableList<Object> possibleValues) {
        this.name = name;
        this.definition = definition;
        this.possibleValues = possibleValues;
    }


    @Override
    public String name() {
        return name;
    }

    @Override
    public ColumnDefinition definition() {
        return definition;
    }

    @Override
    public Optional<ImmutableList<Object>> possibleValues() {
        return Optional.ofNullable(possibleValues);
    }

}
