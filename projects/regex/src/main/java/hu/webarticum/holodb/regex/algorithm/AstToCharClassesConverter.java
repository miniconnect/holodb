package hu.webarticum.holodb.regex.algorithm;

import java.util.function.Predicate;

import hu.webarticum.holodb.regex.ast.BuiltinCharacterClassAstNode;
import hu.webarticum.holodb.regex.ast.CharacterClassAstNode;
import hu.webarticum.holodb.regex.ast.CharacterConstantAstNode;
import hu.webarticum.holodb.regex.ast.CharacterMatchAstNode;
import hu.webarticum.holodb.regex.ast.LinebreakAstNode;
import hu.webarticum.holodb.regex.ast.PosixCharacterClassAstNode;
import hu.webarticum.holodb.regex.ast.RangeAstNode;
import hu.webarticum.holodb.regex.ast.UnicodePropertyCharacterClassAstNode;
import hu.webarticum.holodb.regex.charclass.CharClass;
import hu.webarticum.holodb.regex.comparator.CharComparator;
import hu.webarticum.miniconnect.lang.ImmutableList;

public class AstToCharClassesConverter {
    
    private final CharComparator charComparator;

    public AstToCharClassesConverter(CharComparator charComparator) {
        this.charComparator = charComparator;
    }
    
    public ImmutableList<CharClass> convert(CharacterMatchAstNode astNode) {
        String chars = charsFromCharacterMatch(astNode);
        if (isAlwaysSameType(astNode)) {
            return singletonCharClassesOf(chars);
        } else {
            return separatedCharClassesOf(chars);
        }
    }
    
    private boolean isAlwaysSameType(CharacterMatchAstNode astNode) {
        return
                astNode instanceof CharacterConstantAstNode ||
                astNode instanceof LinebreakAstNode ||
                astNode == BuiltinCharacterClassAstNode.WORD ||
                astNode == BuiltinCharacterClassAstNode.DIGIT ||
                astNode == BuiltinCharacterClassAstNode.WHITESPACE ||
                astNode == BuiltinCharacterClassAstNode.HORIZONTAL_WHITESPACE ||
                astNode == BuiltinCharacterClassAstNode.VERTICAL_WHITESPACE;
    }

    private ImmutableList<CharClass> singletonCharClassesOf(String chars) {
        return ImmutableList.of(CharClass.of(chars, charComparator));
    }

    private ImmutableList<CharClass> separatedCharClassesOf(String chars) {
        return CharAnchorKind.separate(chars).map(s -> CharClass.of(s, charComparator));
    }
    
    private String charsFromCharacterMatch(CharacterMatchAstNode astNode) {
        if (astNode instanceof BuiltinCharacterClassAstNode) {
            return charsFromBuiltinCharacterClass((BuiltinCharacterClassAstNode) astNode);
        } else if (astNode instanceof CharacterClassAstNode) {
            return charsFromCharacterClass((CharacterClassAstNode) astNode);
        } else if (astNode instanceof CharacterConstantAstNode) {
            return charsFromCharacterConstant((CharacterConstantAstNode) astNode);
        } else if (astNode instanceof LinebreakAstNode) {
            return charsFromLineBreak((LinebreakAstNode) astNode);
        } else if (astNode instanceof PosixCharacterClassAstNode) {
            return charsFromPosixCharacterClass((PosixCharacterClassAstNode) astNode);
        } else if (astNode instanceof RangeAstNode) {
            return charsFromRange((RangeAstNode) astNode);
        } else if (astNode instanceof UnicodePropertyCharacterClassAstNode) {
            return charsFromUnicodePropertyProperty((UnicodePropertyCharacterClassAstNode) astNode);
        } else {
            return "";
        }
    }
    
    private String charsFromBuiltinCharacterClass(BuiltinCharacterClassAstNode node) {
        switch (node) {
            case ANY:
                return asciiRange(c -> !Character.isISOControl(c));
            case WORD:
                return asciiRange(c -> c == '_' || Character.isLetterOrDigit(c));
            case NON_WORD:
                return asciiRange(c -> c != '_' && !Character.isISOControl(c) && !Character.isLetterOrDigit(c));
            case DIGIT:
                return asciiRange(Character::isDigit);
            case NON_DIGIT:
                return asciiRange(c -> !Character.isISOControl(c) && !Character.isDigit(c));
            case WHITESPACE:
                return asciiRange(Character::isWhitespace);
            case NON_WHITESPACE:
                return asciiRange(c -> !Character.isISOControl(c) && !Character.isWhitespace(c));
            case HORIZONTAL_WHITESPACE:
                return asciiRange(c -> c == ' ' || c == '\t');
            case NON_HORIZONTAL_WHITESPACE:
                return asciiRange(c -> !Character.isISOControl(c) && c != ' ' && c != '\t');
            case VERTICAL_WHITESPACE:
                return asciiRange(c -> c == '\n' || c == '\r' || c == '\f');
            case NON_VERTICAL_WHITESPACE:
                return asciiRange(c -> !Character.isISOControl(c) && c != '\n' && c != '\r' && c != '\f');
            default:
                return "";
        }
    }

    private String charsFromCharacterClass(CharacterClassAstNode node) {
        StringBuilder resultBuilder = new StringBuilder();
        for (CharacterMatchAstNode innerNode : node.nodes()) {
            resultBuilder.append(charsFromCharacterMatch(innerNode));
        }
        return resultBuilder.toString();
        
    }

    private String charsFromCharacterConstant(CharacterConstantAstNode node) {
        return Character.toString(node.value());
    }
    
    private String charsFromLineBreak(LinebreakAstNode node) {
        return "\n";
    }

    private String charsFromPosixCharacterClass(PosixCharacterClassAstNode node) {
        String positiveSet = charsFromPosixProperty(node.property());
        if (node.positive()) {
            return positiveSet;
        } else {
            return asciiComplementer(positiveSet);
        }
    }
    
    private String charsFromPosixProperty(PosixCharacterClassAstNode.Property property) {
        switch (property) {
            case LOWER:
                return asciiRange(Character::isLowerCase);
            case UPPER:
                return asciiRange(Character::isUpperCase);
            case ASCII:
                return asciiRange(c -> !Character.isISOControl(c));
            case ALPHA:
                return asciiRange(Character::isLetter);
            case DIGIT:
                return asciiRange(Character::isDigit);
            case ALNUM:
                return asciiRange(Character::isLetterOrDigit);
            case PUNCT:
                return asciiRange(c -> !Character.isLetterOrDigit(c) && c >= 33 && c <= 126);
            case GRAPH:
                return asciiRange(c -> c >= 33 && c <= 126);
            case PRINT:
                return asciiRange(c -> Character.isWhitespace(c) || (c >= 33 && c <= 126));
            case BLANK:
                return asciiRange(Character::isWhitespace);
            case CNTRL:
                return asciiRange(Character::isISOControl);
            case XDIGIT:
                return "012345678abcdefABCDEF";
            case SPACE:
                return asciiRange(Character::isWhitespace);
            default:
                return "";
        }
    }

    private String charsFromRange(RangeAstNode node) {
        StringBuilder resultBuilder = new StringBuilder();
        char low = node.low();
        char high = node.high();
        for (char c = low; c <= high; c++) {
            resultBuilder.append(c);
        }
        return resultBuilder.toString();
        
    }

    private String charsFromUnicodePropertyProperty(UnicodePropertyCharacterClassAstNode node) {
        String positiveSet = charsFromUnicodePropertyProperty(node.property());
        if (node.positive()) {
            return positiveSet;
        } else {
            return asciiComplementer(positiveSet);
        }
    }

    private String charsFromUnicodePropertyProperty(UnicodePropertyCharacterClassAstNode.Property property) {
        switch (property) {
            case ALPHABETIC:
                return asciiRange(Character::isLetter);
            case IDEOGRAPHIC:
                
                // FIXME
                return chars('\u4E00');
                
            case LETTER:
                return asciiRange(Character::isLetter);
            case LOWERCASE:
                return asciiRange(Character::isLowerCase);
            case UPPERCASE:
                return asciiRange(Character::isUpperCase);
            case TITLECASE:
                
                // FIXME
                return chars('\u01F3');
                
            case PUNCTUATION:
                return asciiRange(c -> !Character.isLetterOrDigit(c) && c >= 33 && c <= 126);
            case CONTROL:
                return asciiRange(Character::isISOControl);
            case WHITESPACE:
                return asciiRange(Character::isWhitespace);
            case DIGIT:
                return asciiRange(Character::isDigit);
            case HEX_DIGIT:
                return chars("012345678abcdefABCDEF".toCharArray());
            case JOIN_CONTROL:
                return chars('\u200C', '\u200D');
            case NONCHARACTER:
                
                // FIXME
                return chars('\uFFFF');
                
            case ASSIGNED:

                // FIXME
                return asciiRange(c -> !Character.isISOControl(c));
    
            // FIXME
            default:
                return chars('?');
            
        }
    }

    private String asciiComplementer(String positiveSet) {
        return asciiRange(c -> !Character.isISOControl(c) && !positiveSet.contains(Character.toString(c)));
    }
    
    private String chars(char... chars) {
        return String.valueOf(chars);
    }
    
    private String asciiRange(Predicate<Character> predicate) {
        StringBuilder resultBuilder = new StringBuilder();
        for (int i = 0; i < 128; i++) {
            char c = (char) i;
            if (predicate.test(c)) {
                resultBuilder.append(c);
            }
        }
        return resultBuilder.toString();
    }
    
}
