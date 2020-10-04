package hu.webarticum.holodb.ng.core;

public interface Selectable {

    // FIXME / TODO iterate over base/occured values? first value shortcut?
    // FIXME / TODO reverse?
    
    public Selection select(
            Object lowValue, boolean lowInclusive,
            Object highValue, boolean highInclusive);
    
}
