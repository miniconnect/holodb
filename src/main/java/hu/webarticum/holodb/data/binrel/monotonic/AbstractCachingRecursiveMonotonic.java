package hu.webarticum.holodb.data.binrel.monotonic;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import hu.webarticum.holodb.data.selection.Range;

public abstract class AbstractCachingRecursiveMonotonic extends AbstractRecursiveMonotonic {

    private final int cacheDepth;
    
    private final Map<BigInteger, BigInteger> cachedSplitPoints = new HashMap<>();
    
    
    protected AbstractCachingRecursiveMonotonic(BigInteger size, BigInteger imageSize, int cacheDepth) {
        super(size, imageSize);
        this.cacheDepth = cacheDepth;
    }
    

    @Override
    protected BigInteger split(Range range, Range imageRange, BigInteger imageSplitPoint, int level) {
        BigInteger cachedSplitPoint = cachedSplitPoints.get(imageSplitPoint);
        if (cachedSplitPoint != null) {
            return cachedSplitPoint;
        }
        
        BigInteger splitPoint = splitCacheable(range, imageRange, imageSplitPoint, level);
        
        if (level < cacheDepth) {
            cachedSplitPoints.put(imageSplitPoint, splitPoint);
        }
        
        return splitPoint;
    }

    protected abstract BigInteger splitCacheable(Range range, Range imageRange, BigInteger imageSplitPoint, int level);
    
}
