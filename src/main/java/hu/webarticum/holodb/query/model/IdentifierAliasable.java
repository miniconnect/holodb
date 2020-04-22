package hu.webarticum.holodb.query.model;

import java.util.Objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public final class IdentifierAliasable implements Aliasable {

    private final Identifier identifier;
    
    private final String alias;
    

    public IdentifierAliasable(Identifier identifier, String alias) {
        this.identifier = Objects.requireNonNull(identifier, "Identifier can not be null");
        this.alias = alias;
    }


    public Identifier getIdentifier() {
        return identifier;
    }

    @Override
    public boolean hasAlias() {
        return (alias != null);
    }
    
    @Override
    public String getAlias() {
        return alias;
    }
    
    public String calculateAlias() {
        if (alias != null) {
            return alias;
        } else {
            return identifier.getBaseName();
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(identifier)
                .append(alias)
                .build();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof IdentifierAliasable)) {
            return false;
        }
        
        IdentifierAliasable other = (IdentifierAliasable) obj;
        return
                Objects.equals(identifier, other.identifier) &&
                Objects.equals(alias, other.alias);
    }
    
}
