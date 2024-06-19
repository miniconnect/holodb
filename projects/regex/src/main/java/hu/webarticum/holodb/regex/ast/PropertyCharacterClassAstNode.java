package hu.webarticum.holodb.regex.ast;

import java.util.Arrays;
import java.util.Objects;

public class PropertyCharacterClassAstNode implements CharacterMatchAstNode {
    
    public enum Property {

        ALPHABETIC("Alpha", "IsAlphabetic"),
        IDEOGRAPHIC("IsIdeographic"),
        LETTER("IsLetter"),
        LOWERCASE("Lower", "IsLowercase"),
        UPPERCASE("Upper", "IsUppercase"),
        TITLECASE("IsTitlecase"),
        PUNCTUATION("Punct", "IsPunctuation"),
        CONTROL("Cntrl", "IsControl"),
        WHITESPACE("Space", "IsWhite_Space"),
        DIGIT("Digit", "IsDigit"),
        HEX_DIGIT("Hex_Digit"),
        JOIN_CONTROL("Join_Control"),
        NONCHARACTER("Noncharacter_Code_Point"),
        ASSIGNED("Assigned"),
        ASCII("ASCII"),
        ALNUM("Alnum"),
        ASCII_HEX_DIGIT("XDigit"),
        GRAPH("Graph"),
        PRINT("Print"),
        BLANK("Blank"),
        
        ;
        
        private final String[] propertyNames;
        
        private Property(String... propertyNames) {
            this.propertyNames = propertyNames;
        }
        
        public static Property of(String propertyName) {
            for (Property property : values()) {
                if (property.acceptsProperyName(propertyName)) {
                    return property;
                }
            }
            throw new IllegalArgumentException("Unknown property name '" + propertyName + "'");
        }
        
        public boolean acceptsProperyName(String propertyName) {
            for (String name : propertyNames) {
                if (propertyName.equals(name)) {
                    return true;
                }
            }
            return false;
        }
        
        public String[] propertyNames() {
            return Arrays.copyOf(propertyNames, 0);
        }
        
    }
    
    private final Property property;
    
    private final boolean positive;
    
    private PropertyCharacterClassAstNode(Property property, boolean positive) {
        this.property = property;
        this.positive = positive;
    }

    public static PropertyCharacterClassAstNode of(String propertyName, boolean positive) {
        return of(Property.of(propertyName), positive);
    }
    
    public static PropertyCharacterClassAstNode of(Property property, boolean positive) {
        return new PropertyCharacterClassAstNode(property, positive);
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
        } else if (!(obj instanceof PropertyCharacterClassAstNode)) {
            return false;
        }
        PropertyCharacterClassAstNode other = (PropertyCharacterClassAstNode) obj;
        return (
                property == other.property &&
                positive == other.positive);
    }

    @Override
    public String toString() {
        return "uprop{kind:" + property + ", positive: " + positive + "}";
    }
    
}
