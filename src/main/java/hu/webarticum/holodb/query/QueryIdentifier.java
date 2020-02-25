package hu.webarticum.holodb.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QueryIdentifier {

    private final List<String> tokens;
    

    public QueryIdentifier(String... tokens) {
        this(Arrays.asList(tokens));
    }

    public QueryIdentifier(List<String> tokens) {
        this.tokens = new ArrayList<>(tokens);
    }
    
    
    public List<String> getTokens() {
        return new ArrayList<>(tokens);
    }
    
}
