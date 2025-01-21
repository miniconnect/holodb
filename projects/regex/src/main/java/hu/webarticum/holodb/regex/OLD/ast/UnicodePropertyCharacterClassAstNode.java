package hu.webarticum.holodb.regex.OLD.ast;

import java.util.Objects;
import java.util.Optional;

public class UnicodePropertyCharacterClassAstNode implements CharacterMatchAstNode {
    
    public enum Property {

        ALPHABETIC("IsAlphabetic"),
        IDEOGRAPHIC("IsIdeographic"),
        LETTER("IsLetter"),
        LOWERCASE("IsLowercase"),
        UPPERCASE("IsUppercase"),
        TITLECASE("IsTitlecase"),
        PUNCTUATION("IsPunctuation"),
        CONTROL("IsControl"),
        WHITESPACE("IsWhite_Space"),
        DIGIT("IsDigit"),
        HEX_DIGIT("IsHex_Digit"),
        JOIN_CONTROL("IsJoin_Control"),
        NONCHARACTER("IsNoncharacter_Code_Point"),
        ASSIGNED("IsAssigned"),
        
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
    
    private UnicodePropertyCharacterClassAstNode(Property property, boolean positive) {
        this.property = property;
        this.positive = positive;
    }

    public static Optional<UnicodePropertyCharacterClassAstNode> of(String propertyName, boolean positive) {
        Optional<Property> optionalProperty = Property.of(propertyName);
        if (!optionalProperty.isPresent()) {
            return Optional.empty();
        }
        return Optional.of(UnicodePropertyCharacterClassAstNode.of(optionalProperty.get(), positive));
    }
    
    public static UnicodePropertyCharacterClassAstNode of(Property property, boolean positive) {
        return new UnicodePropertyCharacterClassAstNode(property, positive);
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
        } else if (!(obj instanceof UnicodePropertyCharacterClassAstNode)) {
            return false;
        }
        UnicodePropertyCharacterClassAstNode other = (UnicodePropertyCharacterClassAstNode) obj;
        return (
                property == other.property &&
                positive == other.positive);
    }

    @Override
    public String toString() {
        return "uprop{kind:" + property + ", positive: " + positive + "}";
    }
    
}
