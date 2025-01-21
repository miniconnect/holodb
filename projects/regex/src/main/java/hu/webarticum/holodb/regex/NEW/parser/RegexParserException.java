package hu.webarticum.holodb.regex.NEW.parser;

public class RegexParserException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    private final int position;

    public RegexParserException(int position, String message) {
        this(position, message, null);
    }

    public RegexParserException(int position, String message, Throwable cause) {
        super(message, cause);
        this.position = position;
    }
    
    public int position() {
        return position;
    }

}
