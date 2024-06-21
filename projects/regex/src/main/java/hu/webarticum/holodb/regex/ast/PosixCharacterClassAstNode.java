package hu.webarticum.holodb.regex.ast;

import java.util.Objects;
import java.util.Optional;

public class PosixCharacterClassAstNode implements CharacterMatchAstNode {
    
    public enum Property {

        LOWER("Lower"),
        UPPER("Upper"),
        ASCII("ASCII"),
        ALPHA("Alpha"),
        DIGIT("Digit"),
        ALNUM("Alnum"),
        PUNCT("Punct"),
        GRAPH("Graph"),
        PRINT("Print"),
        BLANK("Blank"),
        CNTRL("Cntrl"),
        XDIGIT("XDigit"),
        SPACE("Space"),
        
        ;
        
        private final String propertyName;
        
        private Property(String propertyName) {
            this.propertyName = propertyName;
        }
        
        public static Optional<Property> of(String propertyName) {
            for (Property property : values()) {
                if (property.propertyName.equals(propertyName)) {
                    return Optional.of(property);
                }
            }
            return Optional.empty();
        }
        
        public String propertyName() {
            return propertyName;
        }
        
    }
    
    private final Property property;
    
    private final boolean positive;
    
    private PosixCharacterClassAstNode(Property property, boolean positive) {
        this.property = property;
        this.positive = positive;
    }

    public static Optional<PosixCharacterClassAstNode> of(String propertyName, boolean positive) {
        Optional<Property> optionalProperty = Property.of(propertyName);
        if (!optionalProperty.isPresent()) {
            return Optional.empty();
        }
        return Optional.of(PosixCharacterClassAstNode.of(optionalProperty.get(), positive));
    }
    
    public static PosixCharacterClassAstNode of(Property property, boolean positive) {
        return new PosixCharacterClassAstNode(property, positive);
    }

    public Property property() {
        return property;
    }

    public boolean positive() {
        return positive;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(property, positive);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof PosixCharacterClassAstNode)) {
            return false;
        }
        PosixCharacterClassAstNode other = (PosixCharacterClassAstNode) obj;
        return (
                property == other.property &&
                positive == other.positive);
    }

    @Override
    public String toString() {
        return "posixprop{kind: " + property + ", positive: " + positive + "}";
    }
    
}
