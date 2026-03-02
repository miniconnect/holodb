package hu.webarticum.holodb.benchmark.runner;

import hu.webarticum.holodb.benchmark.matcher.TableHeaderMatcher;
import hu.webarticum.miniconnect.api.MiniColumnHeader;
import hu.webarticum.miniconnect.lang.ImmutableList;

@FunctionalInterface
public interface QueryBenchmarkCaseCallback {

    public void accept(
            String resourcePath,
            String caseName,
            TableHeaderMatcher tableHeaderMatcher,
            ImmutableList<MiniColumnHeader> givenColumnHeaders,
            QueryBenchmarkResult benchmarkResult);

}
