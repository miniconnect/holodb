package hu.webarticum.holodb.core.lab.permutation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Map;
import java.util.function.Function;

import hu.webarticum.holodb.core.data.binrel.permutation.Permutation;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class PermutationDumpMain {

    private static final long PERMUTATION_SIZE = 65536L;

    public static void main(String[] args) throws IOException {
        Map<String, Function<LargeInteger, Permutation>> permutationFactories =
                PermutationFactorySource.createFactories();

        String path;
        if (args.length > 0) {
            path = args[0];
        } else {
            System.out.print("Target directory: ");
            path = new BufferedReader(new InputStreamReader(System.in)).readLine();
        }

        System.out.println(path);
        
        File directory = new File(path);
        directory.mkdirs();
        
        LargeInteger size = LargeInteger.of(PERMUTATION_SIZE);
        for (Map.Entry<String, Function<LargeInteger, Permutation>> entry : permutationFactories.entrySet()) {
            String name = entry.getKey();
            Function<LargeInteger, Permutation> factory = entry.getValue();
            Permutation permutation = factory.apply(size);
            dumpPermutation(directory, name, permutation);
        }
    }

    private static void dumpPermutation(File directory, String name, Permutation permutation) throws IOException {
        File file = new File(directory, name + ".dat");
        System.out.print("Write out " + name + " . . . ");
        try (OutputStream out = new FileOutputStream(file)) {
            for (long i = 0; i < PERMUTATION_SIZE; i++) {
                int value = permutation.at(LargeInteger.of(i)).intValueExact();
                out.write(value & 0xFF);
                out.write((value >> 8) & 0xFF);
            }
        }
        System.out.println("finished");
    }
    
}
