package hu.webarticum.holodb.regex.ast;

public enum BuiltinCharacterClassAstNode implements CharacterMatchAstNode {

    ANY,
    WORD,
    NON_WORD,
    DIGIT,
    NON_DIGIT,
    WHITESPACE,
    NON_WHITESPACE,
    HORIZONTAL_WHITESPACE,
    NON_HORIZONTAL_WHITESPACE,
    VERTICAL_WHITESPACE,
    NON_VERTICAL_WHITESPACE,
    
}
