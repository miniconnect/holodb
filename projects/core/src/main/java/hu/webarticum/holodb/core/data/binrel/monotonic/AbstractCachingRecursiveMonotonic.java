package hu.webarticum.holodb.core.data.binrel.monotonic;

import java.util.HashMap;
import java.util.Map;

import hu.webarticum.holodb.core.data.selection.Range;
import hu.webarticum.miniconnect.lang.LargeInteger;

public abstract class AbstractCachingRecursiveMonotonic extends AbstractRecursiveMonotonic {

    private final int cacheDepth;
    
    private final Map<LargeInteger, LargeInteger> cachedSplitPoints = new HashMap<>();
    
    
    protected AbstractCachingRecursiveMonotonic(LargeInteger size, LargeInteger imageSize, int cacheDepth) {
        super(size, imageSize);
        this.cacheDepth = cacheDepth;
    }
    

    @Override
    protected LargeInteger split(Range range, Range imageRange, LargeInteger imageSplitPoint, int level) {
        LargeInteger cachedSplitPoint = cachedSplitPoints.get(imageSplitPoint);
        if (cachedSplitPoint != null) {
            return cachedSplitPoint;
        }
        
        LargeInteger splitPoint = splitCacheable(range, imageRange, imageSplitPoint, level);
        
        if (level < cacheDepth) {
            cachedSplitPoints.put(imageSplitPoint, splitPoint);
        }
        
        return splitPoint;
    }

    protected abstract LargeInteger splitCacheable(
            Range range, Range imageRange, LargeInteger imageSplitPoint, int level);
    
}
