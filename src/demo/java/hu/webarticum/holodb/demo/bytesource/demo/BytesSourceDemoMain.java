package hu.webarticum.holodb.demo.bytesource.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import hu.webarticum.holodb.data.bitsource.ByteSource;
import hu.webarticum.holodb.data.bitsource.FastByteSource;
import hu.webarticum.holodb.data.bitsource.JavaRandomByteSource;
import hu.webarticum.holodb.demo.util.CommandLineUtil;
import hu.webarticum.holodb.demo.util.MutableHolder;
import hu.webarticum.holodb.util.ByteUtil;

public class BytesSourceDemoMain {

    public static final String TITLE = "ByteSource basic demo";
    

    public static void main(String[] args) throws IOException {
        CommandLineUtil.printTitle(TITLE);
        
        MutableHolder<Integer> seedHolder = new MutableHolder<>();
        
        Pair<Integer, Supplier<ByteSource>> byteSourceUserSelection = CommandLineUtil.readOption("ByteSource implementation", Arrays.asList(
                Pair.of(JavaRandomByteSource.class.getSimpleName(), () -> new JavaRandomByteSource(seedHolder.get())),
                Pair.of(FastByteSource.class.getSimpleName(), () -> new FastByteSource((byte)(int) seedHolder.get()))
                ));
        int byteSourceIndex = byteSourceUserSelection.getLeft();
        Supplier<ByteSource> byteSourceFactory = byteSourceUserSelection.getRight();
        
        if (byteSourceIndex == 1) {
            seedHolder.set(CommandLineUtil.readIntBetween("Seed", 0, 256));
        } else {
            seedHolder.set(CommandLineUtil.readInt("Seed"));
        }
        int previewSize = CommandLineUtil.readIntBetween("Preview size", 1, 1001);
        int dumpSize = CommandLineUtil.readIntBetween("Dump size", 1, 10000001);
        
        
        ByteSource byteSource1 = byteSourceFactory.get();

        CommandLineUtil.printSeparator();

        System.out.println("Preview sample bytes:"); // NOSONAR
        System.out.println(); // NOSONAR

        for (int i = 0; i < previewSize; i++) {
            byte b = byteSource1.next();
            System.out.println( // NOSONAR
                    ByteUtil.byteToBinaryString(b) + "  " +
                    StringUtils.leftPad(Integer.toString(Byte.toUnsignedInt(b)), 3));
        }
        
        CommandLineUtil.printSeparator();
        
        System.out.println("ENT output:"); // NOSONAR
        System.out.println(); // NOSONAR

        ByteSource byteSource2 = byteSourceFactory.get();
        
        File tmpFile = File.createTempFile("bytes-", ".dat");
        OutputStream out = new FileOutputStream(tmpFile); // NOSONAR
        for (int i = 0; i < dumpSize; i++) {
            out.write(byteSource2.next());
        }
        out.close();

        Process process = new ProcessBuilder().command("ent", tmpFile.getAbsolutePath()).start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line); // NOSONAR
        }

        Files.delete(tmpFile.toPath());

        CommandLineUtil.printSeparator();
        
        System.out.println("Finished"); // NOSONAR
    }
    
}
