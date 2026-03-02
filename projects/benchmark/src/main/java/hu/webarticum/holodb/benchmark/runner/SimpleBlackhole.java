package hu.webarticum.holodb.benchmark.runner;

public class SimpleBlackhole { // NOSONAR

    public static volatile Object sink; // NOSONAR

    public static void consume(Object object) {
        sink = object;
    }

}
