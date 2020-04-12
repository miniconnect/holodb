package hu.webarticum.holodb.data.random;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import hu.webarticum.holodb.util.ByteUtil;
import hu.webarticum.holodb.util.bitsource.BitSource;
import hu.webarticum.holodb.util.bitsource.JavaRandomByteSource;

public class DefaultTreeRandom implements TreeRandom {

    private static final int RANDOM_NUMBER_MAX_RETRIES = 15;

    private static final byte SEPARATOR = (byte) 0b11111111;

    private static final byte SEPARATOR_REPLACEMENT = (byte) 0b00000000;

    private static final byte ESCAPER = (byte) 0b11111110;
    
    
    private final DefaultTreeRandom parent;
    
    private final byte[] bytes;
    
    private final Mac mac;
    

    public DefaultTreeRandom(long seed) {
        this(BigInteger.valueOf(seed));
    }
    
    public DefaultTreeRandom(BigInteger seed) {
        this(seed.toByteArray());
    }
    
    public DefaultTreeRandom(byte[] seed) {
        this(null, cleanBytes(seed), buildMac(seed));
    }
    
    private DefaultTreeRandom(DefaultTreeRandom parent, byte[] bytes, Mac mac) {
        this.parent = parent;
        this.bytes = bytes;
        this.mac = mac;
    }


    @Override
    public TreeRandom sub(BigInteger number) {
        return sub(number.toByteArray());
    }

    @Override
    public TreeRandom sub(byte... bytes) {
        byte[] cleanSubBytes = cleanBytes(bytes);
        byte[] bytesForSub = new byte[this.bytes.length + 1 + cleanSubBytes.length];
        System.arraycopy(this.bytes, 0, bytesForSub, 0, this.bytes.length);
        bytesForSub[this.bytes.length] = SEPARATOR;
        System.arraycopy(cleanSubBytes, 0, bytesForSub, this.bytes.length + 1, cleanSubBytes.length);
        return new DefaultTreeRandom(this, bytesForSub, mac);
    }

    @Override
    public byte[] getBytes(int numberOfBytes) {
        return createBitSource().fetch(numberOfBytes * 8);   
    }

    @Override
    public BigInteger getNumber(BigInteger highExclusive) {
        if (highExclusive.signum() != 1) {
            throw new IllegalArgumentException("High value must be positive");
        }
        
        BigInteger two = BigInteger.valueOf(2);
        BigInteger factor = highExclusive;
        BigInteger powerOfTwo = BigInteger.ONE;
        int exponentOfTwo = 0;
        while (factor.mod(two).equals(BigInteger.ZERO)) {
            factor = factor.divide(two);
            powerOfTwo = powerOfTwo.multiply(two);
            exponentOfTwo++;
        }
        int bitCountOfFactor = factor.bitCount();
        
        BitSource bitSource = createBitSource();
        
        BigInteger partition = exponentOfTwo > 0 ? new BigInteger(bitSource.fetch(exponentOfTwo)) : BigInteger.ZERO;
        BigInteger offset = BigInteger.ZERO;
        for (int i = 0; i < RANDOM_NUMBER_MAX_RETRIES; i++) {
            BigInteger offsetCandidate = new BigInteger(bitSource.fetch(bitCountOfFactor));
            if (offsetCandidate.compareTo(factor) < 0) {
                offset = offsetCandidate;
                break;
            }
        }
        
        return partition.multiply(factor).add(offset);
    }

    
    private static Mac buildMac(byte[] bytes) {
        try {
            return buildMacThrows(bytes);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            // never occurs
            throw new RuntimeException(); // NOSONAR
        }
    }
    
    private static Mac buildMacThrows(byte[] bytes) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec key = new SecretKeySpec(bytes, "RawBytes");
        mac.init(key);
        return mac;
    }

    private static byte[] cleanBytes(byte[] bytes) {
        ByteArrayOutputStream bytesBuilder = new ByteArrayOutputStream(bytes.length);
        for (byte b : bytes) {
            if (b == SEPARATOR) {
                bytesBuilder.write(ESCAPER);
                bytesBuilder.write(SEPARATOR_REPLACEMENT);
            } else if (b == ESCAPER) {
                bytesBuilder.write(ESCAPER);
                bytesBuilder.write(ESCAPER);
            } else {
                bytesBuilder.write(b);
            }
        }
        return bytesBuilder.toByteArray();
    }
    
    private BitSource createBitSource() {
        byte[] macBytes = mac.doFinal(bytes);
        Random random = new Random(ByteUtil.firstBytesToLong(macBytes));
        return new BitSource(macBytes, new JavaRandomByteSource(random));
    }

}
