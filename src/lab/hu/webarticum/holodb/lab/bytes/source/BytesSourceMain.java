package hu.webarticum.holodb.lab.bytes.source;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;

import org.apache.commons.lang3.StringUtils;

import hu.webarticum.holodb.lab.util.CommandLineUtil;
import hu.webarticum.holodb.util.ByteUtil;
import hu.webarticum.holodb.util.bitsource.ByteSource;
import hu.webarticum.holodb.util.bitsource.FastByteSource;

public class BytesSourceMain {

    public static void main(String[] args) throws IOException {
        ByteSource byteSource = new FastByteSource();
        
        for (int i = 0; i < 100; i++) {
            byte b = byteSource.next();
            System.out.println( // NOSONAR
                    ByteUtil.byteToBinaryString(b) + "  " +
                    StringUtils.leftPad(Integer.toString(Byte.toUnsignedInt(b)), 3));
        }
        
        CommandLineUtil.printSeparator();
        
        System.out.println("ENT output:"); // NOSONAR
        System.out.println(); // NOSONAR
        
        File tmpFile = File.createTempFile("bytes-", ".dat");
        OutputStream out = new FileOutputStream(tmpFile); // NOSONAR
        for (int i = 0; i < 100000; i++) {
            out.write(byteSource.next());
        }
        out.close();

        Process process = new ProcessBuilder().command("ent", tmpFile.getAbsolutePath()).start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line); // NOSONAR
        }

        Files.delete(tmpFile.toPath());
        System.out.println("Finished"); // NOSONAR
    }
    
}
