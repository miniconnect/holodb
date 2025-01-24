package hu.webarticum.holodb.regex.NEW.algorithm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import hu.webarticum.holodb.regex.NEW.ast.CharacterConstantAstNode;
import hu.webarticum.holodb.regex.NEW.ast.CharacterMatchAstNode;
import hu.webarticum.holodb.regex.NEW.charclass.CharClass;
import hu.webarticum.miniconnect.lang.ImmutableList;

public class AstToCharClassesConverter {
    
    private final Comparator<Character> characterComparator;

    public AstToCharClassesConverter(Comparator<Character> characterComparator) {
        this.characterComparator = characterComparator;
    }
    
    public ImmutableList<CharClass> convert(CharacterMatchAstNode astNode) {
        List<Character> charactersBuilder = new ArrayList<>();
        if (astNode instanceof CharacterConstantAstNode) {
            charactersBuilder.add(((CharacterConstantAstNode) astNode).value());
        } else {
            // TODO, FIXME
            charactersBuilder.add('?');
        }
        return ImmutableList.of(CharClass.of(ImmutableList.fromCollection(charactersBuilder), characterComparator));
    }
    
}
