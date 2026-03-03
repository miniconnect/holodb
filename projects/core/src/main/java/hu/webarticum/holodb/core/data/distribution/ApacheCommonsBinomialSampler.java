package hu.webarticum.holodb.core.data.distribution;

import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;

import hu.webarticum.miniconnect.lang.LargeInteger;

public class ApacheCommonsBinomialSampler implements Sampler {

    private final int size;

    private final BinomialDistribution binomialDistribution;


    public ApacheCommonsBinomialSampler(long seed, int size, double probability) {
        this(createDefaultRandomGenerator(seed), size, probability);
    }

    public ApacheCommonsBinomialSampler(RandomGenerator randomGenerator, int size, double probability) {
        this.size = size;
        this.binomialDistribution = new BinomialDistribution(randomGenerator, size, probability);
    }

    private static RandomGenerator createDefaultRandomGenerator(long seed) {
        return new Well19937c(seed);
    }


    @Override
    public LargeInteger sample() {
        return LargeInteger.of(binomialDistribution.sample());
    }

    @Override
    public LargeInteger size() {
        return LargeInteger.of(size);
    }

}
