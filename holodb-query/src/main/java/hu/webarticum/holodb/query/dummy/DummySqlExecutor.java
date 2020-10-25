package hu.webarticum.holodb.query.dummy;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import hu.webarticum.holodb.core.data.hasher.FastHasher;
import hu.webarticum.holodb.core.data.hasher.Hasher;
import hu.webarticum.holodb.core.data.random.HasherTreeRandom;
import hu.webarticum.holodb.core.data.random.TreeRandom;
import hu.webarticum.holodb.query.common.Result;
import hu.webarticum.holodb.query.common.ResultRow;
import hu.webarticum.holodb.query.common.SqlExecutor;
import hu.webarticum.holodb.query.grammar.SelectLexer;
import hu.webarticum.holodb.query.grammar.SelectParser;
import hu.webarticum.holodb.query.grammar.SelectParser.AliasableCountContext;
import hu.webarticum.holodb.query.grammar.SelectParser.AtomicConditionContext;
import hu.webarticum.holodb.query.grammar.SelectParser.AtomicExpressionContext;
import hu.webarticum.holodb.query.grammar.SelectParser.BinaryConditionContext;
import hu.webarticum.holodb.query.grammar.SelectParser.CompoundableNameContext;
import hu.webarticum.holodb.query.grammar.SelectParser.DecimalLiteralContext;
import hu.webarticum.holodb.query.grammar.SelectParser.ExpressionContext;
import hu.webarticum.holodb.query.grammar.SelectParser.IntegerLiteralContext;
import hu.webarticum.holodb.query.grammar.SelectParser.SelectQueryContext;
import hu.webarticum.holodb.query.grammar.SelectParser.SelectableItemContext;
import hu.webarticum.holodb.query.grammar.SelectParser.SimpleAtomicConditionContext;
import hu.webarticum.holodb.query.grammar.SelectParser.StringLiteralContext;
import hu.webarticum.holodb.query.grammar.SelectParser.WherePartContext;
import hu.webarticum.holodb.query.grammar.SelectParser.WildcardedContext;
import hu.webarticum.holodb.query.util.GrammarUtil;

// TODO: more sophisticated version with more realistic results (date/time, order by etc.)
public class DummySqlExecutor implements SqlExecutor {
    
    private static final Pattern ID_FIELD_PATTERN = Pattern.compile("(?:(?:^|_)[iI][dD]|Id)$");
    
    private static final Pattern INTEGER_FUNC_PATTERN = Pattern.compile(
            "^LEN(?:GTH)$|^COUNT$"); // NOSONAR
    
    private static final Pattern STRING_FUNC_PATTERN = Pattern.compile(
            "(?:^|_)SUBSTR(?:ING)(?:_|$)|(?:^|_)CONCAT$"); // NOSONAR
    
    private static final Set<Class<?>> LITERAL_TYPES = new HashSet<>();
    static{
        LITERAL_TYPES.add(IntegerLiteralContext.class);
        LITERAL_TYPES.add(DecimalLiteralContext.class);
        LITERAL_TYPES.add(StringLiteralContext.class);
    }
    
    private static final String WILDCARD_NAME = "any";
    
    private static final int TABLE_SIZE = 100;
    
    private static final int FILTERED_TABLE_SIZE = 30;
    
    private static final int GROUP_SIZE = 10;
    
    private static final int FILTERED_GROUP_SIZE = 5;
    
    private static final int INTEGER_BOUND = 7200;
    
    private static final int FRACTION_BOUND = 100;
    
    
    private final TreeRandom treeRandom = new HasherTreeRandom();
    

    @Override
    public Result execute(String sql) {
        SelectLexer lexer = new SelectLexer(CharStreams.fromString(sql));
        SelectParser parser = new SelectParser(new CommonTokenStream(lexer));
        SelectQueryContext context = parser.selectQuery();
        
        boolean isExplicitlyGrouped = (context.groupByPart() != null);
        boolean isFiltered = (context.wherePart() != null || context.havingPart() != null);
        boolean isCount = (context.selectPart().countSelection() != null);
        
        int size = TABLE_SIZE;
        int groupSize = 1;
        
        if (isExplicitlyGrouped) {
            size = isFiltered ?
                    FILTERED_TABLE_SIZE / FILTERED_GROUP_SIZE :
                    TABLE_SIZE / GROUP_SIZE;
            groupSize = isFiltered ? FILTERED_GROUP_SIZE : GROUP_SIZE;
        } else if (isCount) {
            size = 1;
            groupSize = isFiltered ? TABLE_SIZE : FILTERED_TABLE_SIZE;
        }
        
        if (context.offsetPart() != null) {
            int offset = Integer.parseInt(context.offsetPart().integerLiteral().getText());
            size = Math.max(0, size - offset);
        }
        
        if (context.limitPart() != null) {
            int limit = Integer.parseInt(context.limitPart().integerLiteral().getText());
            size = Math.min(size, limit);
        }
        
        if (
                size > 1 &&
                groupSize == 1 &&
                isPickedById(context)) {
            size = 1;
        }
        
        Function<Integer, ResultRow> rowFactory = isCount ?
                createCountRowFactory(context, groupSize) :
                createNormalRowFactory(context);
        
        return new DummyResult(new DummyResultSet(size, rowFactory));
    }
    
    private boolean isPickedById(SelectQueryContext context) {
        WherePartContext wherePart = context.wherePart();
        if (
                wherePart == null ||
                wherePart.whereAndConnections() != null &&
                wherePart.whereOrConnections() != null) {
            return false;
        }
        
        SimpleAtomicConditionContext simpleCondition = wherePart.atomicCondition().simpleAtomicCondition();
        if (simpleCondition == null) {
            return false;
        }
        
        BinaryConditionContext binaryCondition = simpleCondition.binaryCondition();
        if (binaryCondition == null || binaryCondition.relationOperator().REL_EQ() == null) {
            return false;
        }
        
        CompoundableNameContext compoundableNameContext = binaryCondition.compoundableName();

        if (compoundableNameContext.parentName != null) {
            String baseTableName = GrammarUtil.extractName(context.fromPart().aliasableName());
            String tableName = GrammarUtil.extractName(compoundableNameContext.parentName);
            if (!tableName.contentEquals(baseTableName)) {
                return false;
            }
        }
        
        String fieldName = GrammarUtil.extractName(compoundableNameContext.selfName);
        
        return ID_FIELD_PATTERN.matcher(fieldName).find();
    }
    
    private Function<Integer, ResultRow> createNormalRowFactory(SelectQueryContext context) {
        Map<String, Function<Integer, Object>> valueFactoryMap = new LinkedHashMap<>();
        Set<String> names = new HashSet<>();
        
        List<SelectableItemContext> items = context.selectPart().normalSelection().selectableItem();
        putItems(valueFactoryMap, items, context, names);
        
        return createRowFactory(valueFactoryMap);
    }

    private Function<Integer, ResultRow> createCountRowFactory(SelectQueryContext context, int groupSize) {
        Map<String, Function<Integer, Object>> valueFactoryMap = new LinkedHashMap<>();
        Set<String> names = new HashSet<>();

        List<SelectableItemContext> beforeItems =
                context.selectPart().countSelection().beforeSelection().selectableItem();
        putItems(valueFactoryMap, beforeItems, context, names);

        AliasableCountContext aliasableCountItem = context.selectPart().countSelection().aliasableCount();
        String name = extractCountName(aliasableCountItem, names);
        Function<Integer, Object> valueFactory = createCountValueFactory(groupSize);
        valueFactoryMap.put(name, valueFactory);

        List<SelectableItemContext> afterItems =
                context.selectPart().countSelection().afterSelection().selectableItem();
        putItems(valueFactoryMap, afterItems, context, names);
        
        return createRowFactory(valueFactoryMap);
    }
    
    private void putItems(
            Map<String, Function<Integer, Object>> valueFactoryMap,
            List<SelectableItemContext> items,
            SelectQueryContext selectQueryContext,
            Set<String> existingNames) {
        
        for (SelectableItemContext itemContext : items) {
            String name = extractNormalName(itemContext, existingNames);
            Function<Integer, Object> valueFactory =
                    createNormalValueFactory(name, itemContext, selectQueryContext);
            valueFactoryMap.put(name, valueFactory);
            existingNames.add(name);
        }
    }

    private String extractNormalName(SelectableItemContext itemContext, Set<String> existingNames) {
        String calculatedName = "";
        
        if (itemContext.aliasableExpression() != null) {
            calculatedName = GrammarUtil.proposeColumnName(itemContext.aliasableExpression());
        } else {
            WildcardedContext wildcarded = itemContext.wildcarded();
            if (wildcarded.parentName != null) {
                calculatedName = GrammarUtil.extractName(wildcarded.parentName) + "_" + WILDCARD_NAME;
            } else {
                calculatedName = WILDCARD_NAME;
            }
        }
        
        return ensureUnique(calculatedName, existingNames);
    }

    private String extractCountName(AliasableCountContext aliasableCountContext, Set<String> existingNames) {
        String calculatedName = GrammarUtil.proposeColumnName(aliasableCountContext);
        return ensureUnique(calculatedName, existingNames);
    }
    
    private String ensureUnique(String proposedName, Set<String> existingNames) {
        if (!existingNames.contains(proposedName)) {
            return proposedName;
        }
        
        int i = 2;
        while (existingNames.contains(proposedName + "_" + i)) {
            i++;
        }
        return proposedName + "_" + i;
    }

    // TODO: refactory, simplify, improve
    private Function<Integer, Object> createNormalValueFactory(
            String columnName, SelectableItemContext itemContext, SelectQueryContext select) {
        
        if (itemContext.wildcarded() != null) {
            return i -> generateString(WILDCARD_NAME, i);
        }
        
        String baseTableName = GrammarUtil.extractName(select.fromPart().aliasableName());

        ExpressionContext expressionContext = itemContext.aliasableExpression().expression();
        if (
                expressionContext.atomicExpression() == null ||
                expressionContext.atomicExpression().compoundableName() == null) {
            return generateExpressionValueFactory(expressionContext, columnName);
        }
        
        CompoundableNameContext compoundableNameContext = expressionContext.atomicExpression().compoundableName();
        String fieldTableName = baseTableName;
        if (compoundableNameContext.parentName != null) {
            fieldTableName = GrammarUtil.extractName(compoundableNameContext.parentName);
        }
        String fieldName = GrammarUtil.extractName(compoundableNameContext.selfName);
        
        WherePartContext wherePart = select.wherePart();
        if (wherePart != null && wherePart.whereOrConnections() == null) {
            List<AtomicConditionContext> conditions = new ArrayList<>();
            conditions.add(wherePart.atomicCondition());
            if (wherePart.whereAndConnections() != null) {
                conditions.addAll(wherePart.whereAndConnections().atomicCondition());
            }
            
            for (AtomicConditionContext condition : conditions) {
                SimpleAtomicConditionContext atomicCondition = condition.simpleAtomicCondition();
                if (atomicCondition != null) {
                    BinaryConditionContext binaryCondition = atomicCondition.binaryCondition();
                    if (binaryCondition != null) {
                        CompoundableNameContext conditionCompoundableNameContext =
                                binaryCondition.compoundableName();
                        
                        String conditionFieldTableName = baseTableName;
                        if (conditionCompoundableNameContext.parentName != null) {
                            conditionFieldTableName = GrammarUtil.extractName(
                                    conditionCompoundableNameContext.parentName);
                        }
                        String conditionFieldName = GrammarUtil.extractName(
                                conditionCompoundableNameContext.selfName);

                        if (
                                conditionFieldTableName.equals(fieldTableName) &&
                                conditionFieldName.equals(fieldName) &&
                                binaryCondition.relationOperator().REL_EQ() != null) {
                            Object literalValue = GrammarUtil.evaluateLiteral(binaryCondition.literal());
                            return i -> literalValue;
                        }
                    }
                }
            }
        }

        if (ID_FIELD_PATTERN.matcher(fieldName).find()) {
            return this::generateId;
        }
        
        return i -> generateString(fieldName, i);
    }

    private BigInteger generateId(int i) {
        return BigInteger.valueOf((i * 3) + 1L);
    }
    
    private Function<Integer, Object> generateExpressionValueFactory(
            ExpressionContext expressionContext, String name) {
        
        AtomicExpressionContext atomicExpression = expressionContext.atomicExpression();
        if (atomicExpression != null) {
            if (atomicExpression.literal() != null) {
                Object value = GrammarUtil.evaluateLiteral(expressionContext.atomicExpression().literal());
                return i -> value;
            } else if (atomicExpression.function() != null) {
                String functionUpperName = atomicExpression.function().simpleName().getText().toUpperCase();
                if (INTEGER_FUNC_PATTERN.matcher(functionUpperName).find()) {
                    return i -> generateInteger(name, i);
                } else if (STRING_FUNC_PATTERN.matcher(functionUpperName).find()) {
                    return i -> generateString(name, i);
                }
            }
        }
        
        Set<Class<?>> literalTypes = detectLiteralTypes(expressionContext);
        
        if (literalTypes.contains(StringLiteralContext.class)) {
            return i -> this.generateString(name, i);
        } else if (literalTypes.contains(DecimalLiteralContext.class)) {
            return i -> this.generateDecimal(name, i);
        } else if (literalTypes.contains(IntegerLiteralContext.class)) {
            return i -> this.generateInteger(name, i);
        } else {
            return i -> this.generateString(name, i);
        }
    }
    
    private Set<Class<?>> detectLiteralTypes(ParseTree tree) {
        Set<Class<?>> result = new HashSet<>();
        
        if (LITERAL_TYPES.contains(tree.getClass())) {
            result.add(tree.getClass());
        } else {
            int count = tree.getChildCount();
            for (int i = 0; i < count; i++) {
                result.addAll(detectLiteralTypes(tree.getChild(i)));
            }
        }
        
        return result;
    }

    private BigInteger generateInteger(String name, int i) {
        return treeRandom.sub(i).sub(name).getNumber(BigInteger.valueOf(INTEGER_BOUND));
    }

    private BigDecimal generateDecimal(String name, int i) {
        BigInteger wholePart = treeRandom.sub(i).sub(name).sub(0).getNumber(BigInteger.valueOf(INTEGER_BOUND));
        BigInteger fractionNumeratorPart = treeRandom.sub(i).sub(name).sub(1).getNumber(BigInteger.valueOf(FRACTION_BOUND));
        BigDecimal fractionPart = new BigDecimal(fractionNumeratorPart).divide(BigDecimal.valueOf(FRACTION_BOUND));
        return new BigDecimal(wholePart).add(fractionPart);
    }

    private String generateString(String name, int i) {
        int length = 5;
        char[] availableChars = "abcdefghijklmnopqrstuvwxyz012345".toCharArray();
        byte[] sourceBytes = treeRandom.sub(i).sub(name).getBytes(length);
        char[] resultChars = new char[length];
        for (int j = 0; j < length; j++) {
            int charIndex = Byte.toUnsignedInt(sourceBytes[j]) / 8;
            resultChars[j] = availableChars[charIndex];
        }
        return new String(resultChars);
        
    }
    
    private Function<Integer, Object> createCountValueFactory(int groupSize) {
        return i -> groupSize;
    }
    
    private Function<Integer, ResultRow> createRowFactory(
            Map<String, Function<Integer, Object>> valueFactoryMap) {
        
        return i -> {
            Map<String, Object> data = new LinkedHashMap<>();
            for (Map.Entry<String, Function<Integer, Object>> entry : valueFactoryMap.entrySet()) {
                data.put(entry.getKey(), entry.getValue().apply(i));
            }
            return DummyResultRow.wrap(data);
        };
    }

}
