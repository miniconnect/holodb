package hu.webarticum.holodb.core.data.distribution;

import java.math.BigInteger;
import java.util.Random;

public class ExperimentalSampler implements Sampler {
    
    private final BigInteger size;
    
    private final double probability;
    
    private final Random random;
    

    public ExperimentalSampler(long seed, BigInteger size, double probability) {
        this.size = size;
        this.probability = probability;
        this.random = new Random(seed);
    }
    
    
    @Override
    public BigInteger sample() {
        
        // TODO
        return null;
        
    }

    /*
    private double getFastSample(double mode, double at) {
        double transformed = (at - 0.5) * 2;
        double powered = FastMath.pow(transformed, ((size().bitLength() / 2) * 2) + 1L); // NOSONAR
        double multiplier = (at < 0.5) ? mode : (1 - mode);
        return (multiplier * powered) + mode;
    }
    */
    
    @Override
    public BigInteger size() {
        return size;
    }

}
