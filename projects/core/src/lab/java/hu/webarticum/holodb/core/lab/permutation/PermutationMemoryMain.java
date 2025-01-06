package hu.webarticum.holodb.core.lab.permutation;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

import org.openjdk.jol.info.GraphLayout;

import hu.webarticum.holodb.core.data.binrel.permutation.Permutation;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class PermutationMemoryMain {

    private static final long PERMUTATION_SIZE = 65536L;

    public static void main(String[] args) throws IOException {
        Map<String, Function<LargeInteger, Permutation>> permutationFactories =
                PermutationFactorySource.createFactories();
        LargeInteger size = LargeInteger.of(PERMUTATION_SIZE);
        for (Map.Entry<String, Function<LargeInteger, Permutation>> entry : permutationFactories.entrySet()) {
            String name = entry.getKey();
            Function<LargeInteger, Permutation> factory = entry.getValue();
            Permutation permutation = factory.apply(size);
            measureMemory(name, permutation);
        }
    }

    private static void measureMemory(String name, Object object) throws IOException {
        GraphLayout layout = GraphLayout.parseInstance(object);
        /*
        Mac mac = null;
        if (object instanceof FeistelNetworkPermutation) {
            Hasher hasher = ((FeistelNetworkPermutation) object).hasher;
            if (hasher instanceof Sha256MacHasher) {
                mac = ((Sha256MacHasher) hasher).mac;
            }
        } else if (object instanceof DirtyFpePermutation) {
            mac = ((DirtyFpePermutation) object).mac;
        }
        GraphLayout subLayout = mac == null ? layout : layout.subtract(GraphLayout.parseInstance(mac));*/
        long size = layout.totalSize();
        //long subSize = subLayout.totalSize();
        //System.out.println(name + ": " + subSize + " (" + size + ")     :: " + object.getClass());
        System.out.println(name + ": " + size + "     :: " + object.getClass());
    }
    
}
