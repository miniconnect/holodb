package hu.webarticum.holodb.query.util;

import java.math.BigInteger;
import java.util.regex.Pattern;

import com.ibm.icu.math.BigDecimal;

import hu.webarticum.holodb.query.grammar.SelectParser.AliasContext;
import hu.webarticum.holodb.query.grammar.SelectParser.AliasableCountContext;
import hu.webarticum.holodb.query.grammar.SelectParser.AliasableExpressionContext;
import hu.webarticum.holodb.query.grammar.SelectParser.AliasableNameContext;
import hu.webarticum.holodb.query.grammar.SelectParser.AtomicExpressionContext;
import hu.webarticum.holodb.query.grammar.SelectParser.CompoundableNameContext;
import hu.webarticum.holodb.query.grammar.SelectParser.DecimalLiteralContext;
import hu.webarticum.holodb.query.grammar.SelectParser.ExpressionContext;
import hu.webarticum.holodb.query.grammar.SelectParser.IntegerLiteralContext;
import hu.webarticum.holodb.query.grammar.SelectParser.LiteralContext;
import hu.webarticum.holodb.query.grammar.SelectParser.NameContext;
import hu.webarticum.holodb.query.grammar.SelectParser.QuotedNameContext;
import hu.webarticum.holodb.query.grammar.SelectParser.StringLiteralContext;

public final class GrammarUtil {
    
    private static final String DEFAULT_COLUMN_NAME = "column";
    
    private static final String DEFAULT_COUNT_COLUMN_NAME = "count";
    
    private static final Pattern NON_WORD_PATTERN = Pattern.compile("\\W+");

    private static final Pattern BEGIN_END_PATTERN = Pattern.compile("^_|_$"); // NOSONAR

    private static final Pattern NAME_ESCAPE_PATTERN = Pattern.compile("(?:\\\\(.)|\"(\"))");

    private static final Pattern STRING_ESCAPE_PATTERN = Pattern.compile("(?:\\\\(.)|'('))");
    

    private GrammarUtil() {
        // utility class
    }
    

    public static String proposeColumnName(AliasableExpressionContext aliasableExpressionContext) {
        if (aliasableExpressionContext.alias() != null) {
            return extractName(aliasableExpressionContext.alias());
        }

        return proposeColumnName(aliasableExpressionContext.expression());
    }
    
    public static String proposeColumnName(ExpressionContext expressionContext) {
        AtomicExpressionContext atomicExpressionContext = expressionContext.atomicExpression();
        if (atomicExpressionContext != null) {
            CompoundableNameContext compoundableNameContext = atomicExpressionContext.compoundableName();
            if (compoundableNameContext != null) {
                return extractName(compoundableNameContext.selfName);
            }
        }
        
        return normalizeColumnName(expressionContext.getText());
    }
    
    public static String proposeColumnName(AliasableCountContext aliasableCountContext) {
        if (aliasableCountContext.alias() != null) {
            return extractName(aliasableCountContext.alias());
        }
        
        return DEFAULT_COUNT_COLUMN_NAME;
    }
    
    public static String normalizeColumnName(String name) {
        String result = NON_WORD_PATTERN.matcher(name).replaceAll("_");
        result = BEGIN_END_PATTERN.matcher(result).replaceAll("");
        result = result.toLowerCase();
        return result.isEmpty() ? DEFAULT_COLUMN_NAME : result;
    }

    public static String extractName(AliasableNameContext aliasableNameContext) {
        if (aliasableNameContext.alias() != null) {
            return extractName(aliasableNameContext.alias());
        }
        
        return extractName(aliasableNameContext.name());
    }
    
    public static String extractName(AliasContext aliasContext) {
        if (aliasContext.simpleName() != null) {
            return aliasContext.simpleName().getText();
        }
        
        return extractName(aliasContext.name());
    }

    public static String extractName(NameContext nameContext) {
        if (nameContext.simpleName() != null) {
            return nameContext.simpleName().getText();
        }
        
        return extractName(nameContext.quotedName());
    }
    
    public static String extractName(QuotedNameContext quotedNameContext) {
        return unquoteName(quotedNameContext.QUOTEDNAME().getText());
    }

    public static String unquoteName(String quotedName) {
        String innerPart = quotedName.substring(1, quotedName.length() - 1);
        return NAME_ESCAPE_PATTERN.matcher(innerPart).replaceAll("$1$2");
    }

    public static Object evaluateLiteral(LiteralContext literalContext) {
        IntegerLiteralContext integerLiteralContext = literalContext.integerLiteral();
        if (integerLiteralContext != null) {
            return new BigInteger(integerLiteralContext.getText());
        }
        
        DecimalLiteralContext decimalLiteralContext = literalContext.decimalLiteral();
        if (decimalLiteralContext != null) {
            return new BigDecimal(decimalLiteralContext.getText());
        }
        
        StringLiteralContext stringLiteralContext = literalContext.stringLiteral();
        if (stringLiteralContext != null) {
            return unquoteString(stringLiteralContext.getText());
        }
        
        // FIXME
        return null;
    }
    
    public static String unquoteString(String quotedString) {
        String innerPart = quotedString.substring(1, quotedString.length() - 1);
        return STRING_ESCAPE_PATTERN.matcher(innerPart).replaceAll("$1$2");
    }
    
}
