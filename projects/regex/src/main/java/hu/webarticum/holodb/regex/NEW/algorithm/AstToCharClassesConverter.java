package hu.webarticum.holodb.regex.NEW.algorithm;

import hu.webarticum.holodb.regex.NEW.ast.CharacterConstantAstNode;
import hu.webarticum.holodb.regex.NEW.ast.CharacterMatchAstNode;
import hu.webarticum.holodb.regex.NEW.charclass.CharClass;
import hu.webarticum.holodb.regex.NEW.charclass.CharComparator;
import hu.webarticum.miniconnect.lang.ImmutableList;

public class AstToCharClassesConverter {
    
    private final CharComparator charComparator;

    public AstToCharClassesConverter(CharComparator charComparator) {
        this.charComparator = charComparator;
    }
    
    public ImmutableList<CharClass> convert(CharacterMatchAstNode astNode) {
        StringBuilder charsBuilder = new StringBuilder();
        if (astNode instanceof CharacterConstantAstNode) {
            charsBuilder.append(((CharacterConstantAstNode) astNode).value());
        } else {
            // TODO, FIXME
            charsBuilder.append('?');
        }
        return ImmutableList.of(CharClass.of(charsBuilder.toString(), charComparator));
    }
    
}
