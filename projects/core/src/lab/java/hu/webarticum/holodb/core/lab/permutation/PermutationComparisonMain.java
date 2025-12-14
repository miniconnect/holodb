package hu.webarticum.holodb.core.lab.permutation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import hu.webarticum.holodb.core.data.binrel.permutation.Permutation;
import hu.webarticum.holodb.core.data.source.FixedSource;
import hu.webarticum.holodb.core.data.source.PermutatedSource;
import hu.webarticum.holodb.core.data.source.Source;
import hu.webarticum.miniconnect.lang.LargeInteger;

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
        Map<String, Function<LargeInteger, Permutation>> permutationFactories =
                PermutationFactorySource.createFactories();

        System.out.println();
        int maxLength =
                permutationFactories.keySet().stream().mapToInt(String::length).max().orElse(0);
        for (Map.Entry<String, Function<LargeInteger, Permutation>> entry : permutationFactories.entrySet()) {
            String name = entry.getKey();
            Function<LargeInteger, Permutation> factory = entry.getValue();
            Permutation permutation = factory.apply(source.size());
            Source<Character> permutatedSource = new PermutatedSource<>(source, permutation);
            System.out.print(String.format("%-" + maxLength + "s | ", name));
            for (int i = 0; i < lineLength ; i++) {
                System.out.print(permutatedSource.get(LargeInteger.of(i)));
            }
            System.out.println(" |");
        }
    }

}
