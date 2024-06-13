package hu.webarticum.holodb.regex.parser;

public class RegexParserException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    private final int position;
    
    public RegexParserException(int position, String message) {
        super(message);
        this.position = position;
    }
    
    public int position() {
        return position;
    }

}
