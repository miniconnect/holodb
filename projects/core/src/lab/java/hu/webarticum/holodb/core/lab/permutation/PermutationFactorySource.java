package hu.webarticum.holodb.core.lab.permutation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import hu.webarticum.holodb.core.data.binrel.permutation.DirtyFpePermutation;
import hu.webarticum.holodb.core.data.binrel.permutation.FeistelNetworkPermutation;
import hu.webarticum.holodb.core.data.binrel.permutation.ModuloPermutation;
import hu.webarticum.holodb.core.data.binrel.permutation.Permutation;
import hu.webarticum.holodb.core.data.binrel.permutation.PermutationComposition;
import hu.webarticum.holodb.core.data.binrel.permutation.SmallPermutation;
import hu.webarticum.holodb.core.data.hasher.FastHasher;
import hu.webarticum.holodb.core.data.hasher.Sha256MacHasher;
import hu.webarticum.holodb.core.data.random.HasherTreeRandom;
import hu.webarticum.holodb.core.data.random.TreeRandom;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class PermutationFactorySource {
    
    private PermutationFactorySource() {
        // static class
    }
    

    public static Map<String, Function<LargeInteger, Permutation>> createFactories() {
        TreeRandom rootRandom = new HasherTreeRandom("lorem", new Sha256MacHasher());
        
        Map<String, Function<LargeInteger, Permutation>> result = new LinkedHashMap<>();

        result.put("FPE1", s -> new DirtyFpePermutation(rootRandom.sub(1L), s));
        //result.put("FPE2", s -> new DirtyFpePermutation(rootRandom.sub(2L), s));
        result.put("MP1", s -> new ModuloPermutation(rootRandom.sub(3L), s));
        //result.put("MP2", s -> new ModuloPermutation(rootRandom.sub(4L), s));
        //result.put("MPX", s -> new PermutationComposition(
        //        new ModuloPermutation(rootRandom.sub(3L), s),
        //        new ModuloPermutation(rootRandom.sub(4324L), s),
        //        new ModuloPermutation(rootRandom.sub(23434784L), s),
        //        new ModuloPermutation(rootRandom.sub(53243567L), s),
        //        new ModuloPermutation(rootRandom.sub(63L), s)));
        result.put("FEI-F-1", s -> new FeistelNetworkPermutation(rootRandom, s.decrement().bitLength(), 1, new FastHasher("", (s.bitLength() / 8) + 1)).resized(s));
        //result.put("FEI-F-2", s -> new FeistelNetworkPermutation(rootRandom, s.decrement().bitLength(), 2, new FastHasher("", (s.bitLength() / 8) + 1)).resized(s));
        //result.put("FEI-F-3", s -> new FeistelNetworkPermutation(rootRandom, s.decrement().bitLength(), 3, new FastHasher("", (s.bitLength() / 8) + 1)).resized(s));
        result.put("FEI-F-4", s -> new FeistelNetworkPermutation(rootRandom, s.decrement().bitLength(), 4, new FastHasher("", (s.bitLength() / 8) + 1)).resized(s));
        //result.put("FEI-F-10", s -> new FeistelNetworkPermutation(rootRandom, s.decrement().bitLength(), 10, new FastHasher("", (s.bitLength() / 8) + 1)).resized(s));
        //result.put("FEI-F-50", s -> new FeistelNetworkPermutation(rootRandom, s.decrement().bitLength(), 50, new FastHasher("", (s.bitLength() / 8) + 1)).resized(s));
        //result.put("FEI-F-1000", s -> new FeistelNetworkPermutation(rootRandom, s.decrement().bitLength(), 1000, new FastHasher("", (s.bitLength() / 8) + 1)).resized(s));
        //result.put("FEI-S-1", s -> new FeistelNetworkPermutation(rootRandom, s.decrement().bitLength(), 1, new Sha256MacHasher()).resized(s));
        result.put("FEI-S-2", s -> new FeistelNetworkPermutation(rootRandom, s.decrement().bitLength(), 2, new Sha256MacHasher()).resized(s));
        //result.put("FEI-S-3", s -> new FeistelNetworkPermutation(rootRandom, s.decrement().bitLength(), 3, new Sha256MacHasher()).resized(s));
        //result.put("FEI-S-4", s -> new FeistelNetworkPermutation(rootRandom, s.decrement().bitLength(), 4, new Sha256MacHasher()).resized(s));
        result.put("SP", s -> new SmallPermutation(rootRandom, s));
        
        return result;
    }
    
}
