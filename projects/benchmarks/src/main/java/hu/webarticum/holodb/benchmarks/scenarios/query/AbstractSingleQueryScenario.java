package hu.webarticum.holodb.benchmarks.scenarios.query;

import hu.webarticum.holodb.benchmarks.framework.SimpleBlackhole;
import hu.webarticum.miniconnect.api.MiniResult;
import hu.webarticum.miniconnect.api.MiniResultSet;
import hu.webarticum.miniconnect.api.MiniSession;
import hu.webarticum.miniconnect.record.ResultRecord;
import hu.webarticum.miniconnect.record.ResultTable;

public abstract class AbstractSingleQueryScenario extends AbstractQueryingScenario {

    @Override
    public String description() {
        return "SQL execution: " + sql();
    }

    @Override
    protected void runWithSession(MiniSession session) {
        MiniResult result = executeOrThrow(session, sql());
        try (MiniResultSet resultSet = result.resultSet()) {
            for (ResultRecord resultRecord : new ResultTable(resultSet)) {
                SimpleBlackhole.consume(resultRecord);
            }
        }
    }

    public abstract String sql();

}
