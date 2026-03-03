package hu.webarticum.holodb.benchmark.matcher;

import hu.webarticum.miniconnect.api.MiniColumnHeader;
import hu.webarticum.miniconnect.lang.ImmutableList;

public class DefaultTableHeaderMatcher implements TableHeaderMatcher {

    private final ImmutableList<ColumnHeaderMatcher> columnHeaderMatchers;

    private DefaultTableHeaderMatcher(ImmutableList<ColumnHeaderMatcher> columnHeaderMatchers) {
        this.columnHeaderMatchers = columnHeaderMatchers;
    }

    public static DefaultTableHeaderMatcher of(ImmutableList<ColumnHeaderMatcher> columnHeaderMatchers) {
        return new DefaultTableHeaderMatcher(columnHeaderMatchers);
    }

    @Override
    public void match(ImmutableList<MiniColumnHeader> givenColumnHeaders) throws Exception {
        int headerCount = givenColumnHeaders.size();
        int tableWidth = columnHeaderMatchers.size();
        if (headerCount != tableWidth) {
            throw new MatchFailedException("column header count: " + headerCount + " != " + tableWidth);
        }
        for (int i = 0; i < tableWidth; i++) {
            try {
                columnHeaderMatchers.get(i).match(givenColumnHeaders.get(i));
            } catch (Exception e) {
                throw MatchFailedException.prefix("at column header " + i + ": ", e);
            }
        }
    }

}
