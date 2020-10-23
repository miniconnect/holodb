package hu.webarticum.holodb.query.lab;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import hu.webarticum.holodb.query.grammar.SimpleSelectBaseListener;

public class SimpleSelectCompilerListener extends SimpleSelectBaseListener {

    @Override
    public void enterEveryRule(ParserRuleContext ctx) {
        System.out.println(String.format("%s ---> %s  (%s)", textOf(ctx.getStart()), textOf(ctx.getStop()), ctx.getClass().getSimpleName()));
    }
    
    private String textOf(Token token) {
        return token != null ? token.getText() : "---";
    }
    
}
