package hu.webarticum.holodb.regex.graph.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;
import java.util.function.Predicate;

import hu.webarticum.holodb.regex.ast.AnchorAstNode;
import hu.webarticum.holodb.regex.ast.BackreferenceAstNode;
import hu.webarticum.holodb.regex.ast.BuiltinCharacterClassAstNode;
import hu.webarticum.holodb.regex.ast.CharacterClassAstNode;
import hu.webarticum.holodb.regex.ast.CharacterConstantAstNode;
import hu.webarticum.holodb.regex.ast.CharacterMatchAstNode;
import hu.webarticum.holodb.regex.ast.FixedStringAstNode;
import hu.webarticum.holodb.regex.ast.NamedBackreferenceAstNode;
import hu.webarticum.holodb.regex.ast.PosixCharacterClassAstNode;
import hu.webarticum.holodb.regex.ast.RangeAstNode;
import hu.webarticum.holodb.regex.ast.UnicodePropertyCharacterClassAstNode;
import hu.webarticum.holodb.regex.graph.data.CharacterValue;
import hu.webarticum.holodb.regex.graph.data.EmptyNodeData;
import hu.webarticum.holodb.regex.graph.data.MutableNode;
import hu.webarticum.holodb.regex.graph.data.NodeData;
import hu.webarticum.holodb.regex.graph.data.SortedValueSetNodeData;
import hu.webarticum.miniconnect.lang.ImmutableList;

public class GraphCharacterTransformer {
    
    public void transform(MutableNode node) {
        if (node.value instanceof FixedStringAstNode) {
            transformFixedString(node);
        } else {
            node.value = convertValue(node.value);
            for (MutableNode child : node.children) {
                transform(child);
            }
        }
    }

    private Object transformFixedString(MutableNode node) {
        String fixedString = ((FixedStringAstNode) node.value).value();
        int length = fixedString.length();
        if (length == 0) {
            node.value = EmptyNodeData.EMPTY;
            return node;
        } else if (length == 1) {
            node.value = valueSetOf(chars(fixedString.charAt(0)));
            return node;
        }
        MutableNode headNode = new MutableNode(valueSetOf(chars(fixedString.charAt(length - 1))));
        headNode.children = node.children;
        for (int i = length - 2; i >= 1; i--) {
            NodeData valueSet = valueSetOf(chars(fixedString.charAt(i)));
            headNode = new MutableNode(valueSet, headNode);
        }
        node.value = valueSetOf(chars(fixedString.charAt(0)));
        node.children = new ArrayList<>(Arrays.asList(headNode));
        return node;
    }

    private NodeData convertValue(Object value) {
        if (value instanceof NodeData) {
            return (NodeData) value;
        } else if (value == null) {
            return EmptyNodeData.EMPTY;
        } else if (value instanceof AnchorAstNode) {
            
            // FIXME
            return EmptyNodeData.EMPTY;

        } else if (value instanceof BackreferenceAstNode || value instanceof NamedBackreferenceAstNode) {
            
            // FIXME
            throw new IllegalArgumentException("Currently, backreferences are not supported");
            
        } else {
            return valueSetOf(extractCharacterSet(value));
        }
    }
    
    private TreeSet<Character> extractCharacterSet(Object value) {
        if (value instanceof BuiltinCharacterClassAstNode) {
            return extractFromBuiltinCharacterClass((BuiltinCharacterClassAstNode) value);
        } else if (value instanceof CharacterClassAstNode) {
            return extractFromCharacterClass((CharacterClassAstNode) value);
        } else if (value instanceof CharacterConstantAstNode) {
            return extractFromCharacterConstant((CharacterConstantAstNode) value);
        } else if (value instanceof PosixCharacterClassAstNode) {
            return extractFromPosixCharacterClass((PosixCharacterClassAstNode) value);
        } else if (value instanceof RangeAstNode) {
            return extractFromRange((RangeAstNode) value);
        } else if (value instanceof UnicodePropertyCharacterClassAstNode) {
            return extractFromUnicodePropertyCharacterClass((UnicodePropertyCharacterClassAstNode) value);
        } else {
            
            // FIXME
            return chars('?');
            
        }
    }
    
    private TreeSet<Character> extractFromBuiltinCharacterClass(BuiltinCharacterClassAstNode node) {
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
                
            // FIXME
            default:
                return chars('?');
            
        }
    }

    private TreeSet<Character> extractFromCharacterClass(CharacterClassAstNode node) {
        ImmutableList<CharacterMatchAstNode> nodes = node.nodes();
        if (nodes.isEmpty()) {
            
            // FIXME
            return chars('?');
            
        }
        TreeSet<Character> characterSet = extractCharacterSet(nodes.get(0));
        int size = nodes.size();
        for (int i = 1; i < size; i++) {
            characterSet.retainAll(extractCharacterSet(nodes.get(i)));
        }
        return characterSet;
        
    }

    private TreeSet<Character> extractFromCharacterConstant(CharacterConstantAstNode node) {
        return chars(node.value());
    }

    private TreeSet<Character> extractFromPosixCharacterClass(PosixCharacterClassAstNode node) {
        TreeSet<Character> positiveSet = extractFromPosixProperty(node.property());
        if (node.positive()) {
            return positiveSet;
        } else {
            return asciiComplementer(positiveSet);
        }
    }
    
    private TreeSet<Character> extractFromPosixProperty(PosixCharacterClassAstNode.Property property) {
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
                return chars("012345678abcdefABCDEF".toCharArray());
            case SPACE:
                return asciiRange(Character::isWhitespace);

            // FIXME
            default:
                return chars('?');
            
        }
    }

    private TreeSet<Character> extractFromRange(RangeAstNode node) {
        TreeSet<Character> result = new TreeSet<>();
        char low = node.low();
        char high = node.high();
        for (char c = low; c <= high; c++) {
            result.add(c);
        }
        return result;
        
    }

    private TreeSet<Character> extractFromUnicodePropertyCharacterClass(UnicodePropertyCharacterClassAstNode node) {
        TreeSet<Character> positiveSet = extractFromUnicodeProperty(node.property());
        if (node.positive()) {
            return positiveSet;
        } else {
            return asciiComplementer(positiveSet);
        }
    }

    private TreeSet<Character> extractFromUnicodeProperty(UnicodePropertyCharacterClassAstNode.Property property) {
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

    private SortedValueSetNodeData valueSetOf(TreeSet<Character> characterSet) {
        return new SortedValueSetNodeData(ImmutableList.fromCollection(characterSet).map(CharacterValue::new));
    }

    // FIXME
    private TreeSet<Character> asciiComplementer(TreeSet<Character> positiveSet) {
        TreeSet<Character> result = asciiRange(c -> !Character.isISOControl(c));
        result.removeAll(positiveSet);
        if (result.isEmpty()) {
            
            // FIXME
            return chars('?');
            
        }
        return result;
    }
    
    private TreeSet<Character> chars(char... chars) {
        TreeSet<Character> result = new TreeSet<>();
        for (char c : chars) {
            result.add(c);
        }
        return result;
    }
    
    private TreeSet<Character> asciiRange(Predicate<Character> predicate) {
        TreeSet<Character> characterSet = new TreeSet<>();
        for (int i = 0; i < 128; i++) {
            char c = (char) i;
            if (predicate.test(c)) {
                characterSet.add(c);
            }
        }
        return characterSet;
    }
    
}
