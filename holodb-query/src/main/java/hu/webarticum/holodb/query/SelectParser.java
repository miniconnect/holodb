package hu.webarticum.holodb.query;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SelectParser {
    
    private static final String SIMPLE = "[a-z]\\w+";
    
    private static final String QUOTED = "\"[a-z](?:\\\\.|\"\"|[^\"\\\\])+\"";
    
    private static final String NAME = "(?:" + SIMPLE + "|" + QUOTED + ")";

    private static final String QUALIFIED = NAME + "\\s*\\.\\s*" + NAME;

    private static final String IDENTIFIER = "(?:" + NAME + "\\s*\\.\\s*)?" + NAME;

    private static final String ALIASABLE = IDENTIFIER + "(?:\\s+AS\\s+" + NAME + "|\\s+" + QUOTED + ")?";
    
    private static final String LITERAL = "(?:\\d+(?:\\.\\d+)?|'(\\\\.|''|[^'])*')";
    
    private static final String COMPARE = IDENTIFIER + "\\s*(?:[<>=]|[<>]=)\\s*" + LITERAL;
    
    private static final String BETWEEN = IDENTIFIER + "\\s+BETWEEN\\s+" + LITERAL + "\\s+AND\\s+" + LITERAL;
    
    private static final String CONDITION = "(?:" + COMPARE + "|" + BETWEEN + ")";
    
    private static final Pattern PATTERN = Pattern.compile(
            "SELECT\\s+" +
            "(?:(?<selectallcol>\\*)|(?<selectcount>COUNT\\s*\\(\\s*\\*\\s*\\))|" +
            "(?<selectcols>" + ALIASABLE + "(?:\\s*,\\s*" + ALIASABLE + ")*)" +
            ")" +
            "\\s+FROM\\s+(?<from>" + ALIASABLE + ")" +
            "(?<leftjoin>\\s+LEFT\\s+JOIN\\s+ON\\s+" + QUALIFIED + "\\s*=\\s*" + QUALIFIED + ")?" +
            "(?:\\s+WHERE\\s+" +
            "(?<condition1>" + CONDITION + ")" +
            "(?:\\s+(?<operator>AND|OR)\\s+(?<condition2>" + CONDITION + "))?" +
            ")?" +
            "(?:\\s+ORDER\\s+BY\\s+(?<orderby>" + IDENTIFIER + ")(?:\\s+(?<orderdir>ASC|DESC)?)?)?",
            Pattern.CASE_INSENSITIVE);
    
    
    // FIXME
    public static void main(String[] args) {
        String sql = "SELECT col1 AS c1, \"alma\" . \"ko\"\"rte\" FROM table" +
                " WHERE col1 = 'al\\'ma' ORDER BY col2 DESC";
        Matcher matcher = PATTERN.matcher(sql);
        if (matcher.find()) {
            System.out.println(matcher.group("selectcols"));
            System.out.println(matcher.group("from"));
            System.out.println(matcher.group("condition1"));
            System.out.println(matcher.group("orderby"));
            System.out.println(matcher.group("orderdir"));
        } else {
            System.err.println("NO MATCH!");
        }
    }
    
}
