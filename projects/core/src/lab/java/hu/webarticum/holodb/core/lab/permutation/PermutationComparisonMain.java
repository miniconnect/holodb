package hu.webarticum.holodb.core.lab.permutation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import hu.webarticum.holodb.core.data.binrel.permutation.DirtyFpePermutation;
import hu.webarticum.holodb.core.data.binrel.permutation.ModuloPermutation;
import hu.webarticum.holodb.core.data.binrel.permutation.Permutation;
import hu.webarticum.holodb.core.data.hasher.Sha256MacHasher;
import hu.webarticum.holodb.core.data.random.HasherTreeRandom;
import hu.webarticum.holodb.core.data.random.TreeRandom;
import hu.webarticum.holodb.core.data.source.FixedSource;
import hu.webarticum.holodb.core.data.source.PermutatedSource;
import hu.webarticum.holodb.core.data.source.Source;

public class PermutationComparisonMain {

    public static void main(String[] args) throws IOException {
        System.out.print("Characters: ");
        String line = new BufferedReader(new InputStreamReader(System.in)).readLine();
        
        int lineLength = line.length();
        FixedSource<Character> source = new FixedSource<>(
                Character.class,
                line.chars()
                        .mapToObj(c -> Character.valueOf((char) c))
                        .collect(Collectors.toList()));
        Map<String, Function<BigInteger, Permutation>> permutationFactories = createFactories();
        
        System.out.println();
        int maxLength =
                permutationFactories.keySet().stream().mapToInt(String::length).max().orElse(0);
        for (Map.Entry<String, Function<BigInteger, Permutation>> entry : permutationFactories.entrySet()) {
            String name = entry.getKey();
            Function<BigInteger, Permutation> factory = entry.getValue();
            Permutation permutation = factory.apply(source.size());
            Source<Character> permutatedSource = new PermutatedSource<>(source, permutation);
            System.out.print(String.format("%-" + maxLength + "s | ", name));
            for (int i = 0; i < lineLength ; i++) {
                System.out.print(permutatedSource.get(BigInteger.valueOf(i)));
            }
            System.out.println(" |");
        }
    }

    private static Map<String, Function<BigInteger, Permutation>> createFactories() {
        TreeRandom rootRandom = new HasherTreeRandom("lorem", new Sha256MacHasher());
        
        Map<String, Function<BigInteger, Permutation>> result = new LinkedHashMap<>();
        result.put("FPE1", s -> new DirtyFpePermutation(rootRandom.sub(1L), s));
        result.put("FPE2", s -> new DirtyFpePermutation(rootRandom.sub(2L), s));
        result.put("MP1", s -> new ModuloPermutation(rootRandom.sub(3L), s));
        result.put("MP2", s -> new ModuloPermutation(rootRandom.sub(4L), s));
        
        return result;
    }
    
}
