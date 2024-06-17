package hu.webarticum.holodb.regex.ast;

public enum AnchorAstNode implements AstNode {

    WORD_BOUNDARY,
    NON_WORD_BOUNDARY,
    BEGIN_OF_LINE,
    END_OF_LINE,
    BEGIN_OF_INPUT,
    END_OF_INPUT,
    END_OF_INPUT_ALLOW_NEWLINE,
    END_OF_PREVIOUS_MATCH,

}
