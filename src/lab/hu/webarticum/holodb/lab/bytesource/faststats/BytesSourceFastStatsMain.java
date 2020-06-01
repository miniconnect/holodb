package hu.webarticum.holodb.lab.bytesource.faststats;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hu.webarticum.holodb.lab.util.CommandLineUtil;
import hu.webarticum.holodb.util.bitsource.ByteSource;
import hu.webarticum.holodb.util.bitsource.FastByteSource;

//TODO: split code
//TODO: use BigDecimal etc.
// TODO: present in a pretty data table
// TODO: print summary too
public class BytesSourceFastStatsMain {

    public static final String TITLE = "ByteSource statistics";


    private static final Pattern ENT_CORRELATION_PATTERN = Pattern.compile("^Serial correlation coefficient is (\\-?[0-9\\.]+) ");

    private static final Pattern ENT_COMPRESS_PATTERN = Pattern.compile(" file by (\\-?[0-9\\.]+) percent.");
    

    public static void main(String[] args) throws IOException {
        CommandLineUtil.printTitle(TITLE);
        
        int dumpSize = CommandLineUtil.readIntBetween("Dump size", 1, 10000001);
        
        for (int seed = 0; seed < 256; seed++) {
            System.out.println(); // NOSONAR
            System.out.println("SEED: " + seed); // NOSONAR
            
            
            ByteSource byteSource1 = new FastByteSource((byte) seed);

            Set<Byte> remainingBytes = new HashSet<>();
            for (int i = 0; i < 256; i++) {
                remainingBytes.add((byte) i);
            }
            for (int i = 0; i < 5001; i++) { // NOSONAR
                byte b = byteSource1.next();
                if (remainingBytes.remove(b) && remainingBytes.isEmpty()) {
                    System.out.println(String.format("All at: %d", i)); // NOSONAR
                    break;
                } else if (i == 5000) {
                    System.out.println(String.format("OOPS: 5000! (remaining: %s)", remainingBytes)); // NOSONAR
                    break;
                }
            }
            
            
            ByteSource byteSource2 = new FastByteSource((byte) seed);
            
            File tmpFile = File.createTempFile("bytes-", ".dat");
            try (OutputStream out = new FileOutputStream(tmpFile)) {
                for (int i = 0; i < dumpSize; i++) {
                    out.write(byteSource2.next());
                }
            }
    
            Process process = new ProcessBuilder().command("ent", tmpFile.getAbsolutePath()).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher correlationMatcher = ENT_CORRELATION_PATTERN.matcher(line);
                if (correlationMatcher.find()) {
                    System.out.println(String.format("Correlation: %s", correlationMatcher.group(1))); // NOSONAR
                } else {
                    Matcher compressMatcher = ENT_COMPRESS_PATTERN.matcher(line);
                    if (compressMatcher.find()) {
                        System.out.println(String.format("Compress: %s", compressMatcher.group(1))); // NOSONAR
                    }
                }
            }
    
            Files.delete(tmpFile.toPath());
        }
        
        System.out.println("Finished"); // NOSONAR
    }
    
}
