package hu.webarticum.holodb.benchmark.matcher;

public class MatchFailedException extends RuntimeException {

    public MatchFailedException(String message) {
        super(message);
    }

    public MatchFailedException(String message, Throwable e) {
        super(message, e);
    }

    public static MatchFailedException prefix(String prefix, Throwable e) {
        return new MatchFailedException(prefix + extractSubMessage(e), e);
    }

    private static String extractSubMessage(Throwable e) {
        if (!(e instanceof MatchFailedException)) {
            return e.getClass().getSimpleName();
        }

        return ((MatchFailedException) e).getMessage();
    }

}
