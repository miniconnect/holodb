package hu.webarticum.holodb.benchmark.matcher;

import hu.webarticum.miniconnect.api.MiniColumnHeader;

@FunctionalInterface
public interface ColumnHeaderMatcher {

    public boolean isMatching(MiniColumnHeader givenColumnHeader) throws Exception;

    public default void match(MiniColumnHeader givenColumnHeader) throws Exception {
        if (!isMatching(givenColumnHeader)) {
            throw new MatchFailedException("mismatching column header");
        }
    }

}
