package hu.webarticum.holodb.core.lab.bytesource.faststats;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hu.webarticum.holodb.core.data.bitsource.ByteSource;
import hu.webarticum.holodb.core.data.bitsource.FastByteSource;
import hu.webarticum.holodb.core.lab.util.CommandLineUtil;

public class BytesSourceFastStatsMain {

    public static final String TITLE = "ByteSource statistics";
    
    private static final String TABLE_EDGE_LINE = "+-----------------------------------------+";
    
    private static final String TABLE_INNER_LINE = "|------+--------+------------+------------|";

    private static final String TABLE_TITLE_ROW_FORMAT = "| %4s | %6s | %10s | %10s |";
    
    private static final String TABLE_ROW_FORMAT = "| %4d | %6d | %10s | %10s |";
    
    private static final Pattern ENT_CORRELATION_PATTERN = Pattern.compile("^Serial correlation coefficient is (\\-?[0-9\\.]+) ");

    private static final Pattern ENT_COMPRESS_PATTERN = Pattern.compile(" file by (\\-?[0-9\\.]+) percent.");
    

    public static void main(String[] args) throws IOException { // NOSONAR: complexity is OK
        CommandLineUtil.printTitle(TITLE);
        
        int dumpSize = CommandLineUtil.readIntBetween("Dump size", 1, 10000001);
        
        System.out.println(); // NOSONAR
        
        System.out.println(TABLE_EDGE_LINE); // NOSONAR
        System.out.println(String.format(TABLE_TITLE_ROW_FORMAT, "Seed", "All at", "C", "D")); // NOSONAR
        
        int minAllAt = Integer.MAX_VALUE;
        int maxAllAt = Integer.MIN_VALUE;
        BigDecimal absMinCorrelation = null;
        BigDecimal absMaxCorrelation = null;
        BigDecimal minCompressableness = null;
        BigDecimal maxCompressableness = null;
        
        for (int seed = 0; seed < 256; seed++) {
            ByteSource byteSource1 = new FastByteSource((byte) seed);

            int allAt = -1;
            
            Set<Byte> remainingBytes = new HashSet<>();
            for (int i = 0; i < 256; i++) {
                remainingBytes.add((byte) i);
            }
            for (int i = 0; i < 5001; i++) { // NOSONAR
                byte b = byteSource1.next();
                if (remainingBytes.remove(b) && remainingBytes.isEmpty()) {
                    allAt = i;
                    break;
                } else if (i == 5000) {
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

            BigDecimal correlation = null;
            BigDecimal compressableness = null;
            
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher correlationMatcher = ENT_CORRELATION_PATTERN.matcher(line);
                if (correlationMatcher.find()) {
                    correlation = new BigDecimal(correlationMatcher.group(1));
                } else {
                    Matcher compressMatcher = ENT_COMPRESS_PATTERN.matcher(line);
                    if (compressMatcher.find()) {
                        compressableness = new BigDecimal(compressMatcher.group(1));
                    }
                }
            }
    
            Files.delete(tmpFile.toPath());

            System.out.println(TABLE_INNER_LINE); // NOSONAR
            System.out.println(String.format(TABLE_ROW_FORMAT, seed, allAt, correlation, compressableness)); // NOSONAR
            
            if (allAt < minAllAt) {
                minAllAt = allAt;
            }
            if (allAt > maxAllAt) {
                maxAllAt = allAt;
            }
            if (correlation != null && (absMinCorrelation == null || correlation.abs().compareTo(absMinCorrelation.abs()) < 0)) {
                absMinCorrelation = correlation;
            }
            if (correlation != null && (absMaxCorrelation == null || correlation.abs().compareTo(absMaxCorrelation.abs()) > 0)) {
                absMaxCorrelation = correlation;
            }
            if (compressableness != null && (minCompressableness == null || compressableness.compareTo(minCompressableness) < 0)) {
                minCompressableness = compressableness;
            }
            if (compressableness != null && (maxCompressableness == null || compressableness.compareTo(maxCompressableness) > 0)) {
                maxCompressableness = compressableness;
            }
        }
        
        System.out.println(TABLE_EDGE_LINE); // NOSONAR
        System.out.println(); // NOSONAR

        System.out.println(String.format("All at (min): %d", minAllAt)); // NOSONAR
        System.out.println(String.format("All at (max): %d", maxAllAt)); // NOSONAR
        System.out.println(String.format("Correlation (abs min): %s", absMinCorrelation)); // NOSONAR
        System.out.println(String.format("Correlation (abs max): %s", absMaxCorrelation)); // NOSONAR
        System.out.println(String.format("Compressableness (min): %s", minCompressableness)); // NOSONAR
        System.out.println(String.format("Compressableness (max): %s", maxCompressableness)); // NOSONAR
    }
    
}
