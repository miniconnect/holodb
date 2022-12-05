package hu.webarticum.holodb.core.data.random;

import java.io.ByteArrayOutputStream;
import java.util.function.BiFunction;

import hu.webarticum.holodb.core.data.bitsource.ByteSource;
import hu.webarticum.holodb.core.data.bitsource.ByteSourceBitSource;
import hu.webarticum.holodb.core.data.bitsource.FastByteSource;
import hu.webarticum.holodb.core.data.hasher.FastHasher;
import hu.webarticum.holodb.core.data.hasher.Hasher;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class HasherTreeRandom implements TreeRandom {

    private static final int RANDOM_NUMBER_MAX_RETRIES = 15;

    private static final byte SEPARATOR = (byte) 0b11111111;

    private static final byte SEPARATOR_REPLACEMENT = (byte) 0b00000000;

    private static final byte ESCAPER = (byte) 0b11111110;
    
    
    private final byte[] bytes;
    
    private final Hasher hasher;
    
    private final BiFunction<byte[], byte[], ByteSource> additionalByteSourceFactory;
    

    public HasherTreeRandom() {
        this(0L);
    }

    public HasherTreeRandom(long seed) {
        this(LargeInteger.of(seed));
    }

    public HasherTreeRandom(LargeInteger seed) {
        this(seed.toByteArray());
    }

    public HasherTreeRandom(String seed) {
        this(seed.getBytes());
    }

    public HasherTreeRandom(byte[] seed) {
        this(seed, createDefaultHasher(seed));
    }
    
    public HasherTreeRandom(Hasher hasher) {
        this(0L, hasher);
    }

    public HasherTreeRandom(long seed, Hasher hasher) {
        this(LargeInteger.of(seed), hasher);
    }

    public HasherTreeRandom(LargeInteger seed, Hasher hasher) {
        this(seed.toByteArray(), hasher);
    }

    public HasherTreeRandom(String seed, Hasher hasher) {
        this(seed.getBytes(), hasher);
    }
    
    public HasherTreeRandom(byte[] seed, Hasher hasher) {
        this(seed, hasher, createDefaultAdditionalByteSourceFactory());
    }

    public HasherTreeRandom(long seed, Hasher hasher, BiFunction<byte[], byte[], ByteSource> additionalByteSourceFactory) {
        this(LargeInteger.of(seed), hasher, additionalByteSourceFactory);
    }

    public HasherTreeRandom(LargeInteger seed, Hasher hasher, BiFunction<byte[], byte[], ByteSource> additionalByteSourceFactory) {
        this(seed.toByteArray(), hasher, additionalByteSourceFactory);
    }

    public HasherTreeRandom(String seed, Hasher hasher, BiFunction<byte[], byte[], ByteSource> additionalByteSourceFactory) {
        this(seed.getBytes(), hasher, additionalByteSourceFactory);
    }
    
    public HasherTreeRandom(byte[] seed, Hasher hasher, BiFunction<byte[], byte[], ByteSource> additionalByteSourceFactory) {
        this(seed, hasher, additionalByteSourceFactory, true);
    }
    
    private HasherTreeRandom(byte[] bytes, Hasher hasher, BiFunction<byte[], byte[], ByteSource> additionalByteSourceFactory, boolean cleanBytes) {
        this.bytes = cleanBytes ? cleanBytes(bytes) : bytes;
        this.hasher = hasher;
        this.additionalByteSourceFactory = additionalByteSourceFactory;
    }

    private static Hasher createDefaultHasher(byte[] seed) {
        return new FastHasher(seed);
    }

    private static BiFunction<byte[], byte[], ByteSource> createDefaultAdditionalByteSourceFactory() {
        return (treeBytes, hashBytes) -> new FastByteSource(hashBytes[0]);
    }


    @Override
    public TreeRandom sub(byte... bytes) {
        byte[] cleanSubBytes = cleanBytes(bytes);
        byte[] bytesForSub = new byte[this.bytes.length + 1 + cleanSubBytes.length];
        System.arraycopy(this.bytes, 0, bytesForSub, 0, this.bytes.length);
        bytesForSub[this.bytes.length] = SEPARATOR;
        System.arraycopy(cleanSubBytes, 0, bytesForSub, this.bytes.length + 1, cleanSubBytes.length);
        return new HasherTreeRandom(bytesForSub, hasher, additionalByteSourceFactory, false);
    }

    @Override
    public byte[] getBytes(int numberOfBytes) {
        return createBitSource().fetch(numberOfBytes * 8);   
    }

    @Override
    public LargeInteger getNumber(LargeInteger highExclusive) {
        if (highExclusive.signum() != 1) {
            throw new IllegalArgumentException("High value must be positive");
        }
        
        LargeInteger two = LargeInteger.of(2);
        LargeInteger factor = highExclusive;
        LargeInteger powerOfTwo = LargeInteger.ONE;
        int exponentOfTwo = 0;
        while (factor.mod(two).equals(LargeInteger.ZERO)) {
            factor = factor.divide(two);
            powerOfTwo = powerOfTwo.multiply(two);
            exponentOfTwo++;
        }
        int bitCountOfFactor = factor.bitCount();
        
        ByteSourceBitSource bitSource = createBitSource();
        
        LargeInteger partition = exponentOfTwo > 0 ? LargeInteger.of(bitSource.fetch(exponentOfTwo)) : LargeInteger.ZERO;
        LargeInteger offset = LargeInteger.ZERO;
        for (int i = 0; i < RANDOM_NUMBER_MAX_RETRIES; i++) {
            LargeInteger offsetCandidate = LargeInteger.of(bitSource.fetch(bitCountOfFactor));
            if (offsetCandidate.compareTo(factor) < 0) {
                offset = offsetCandidate;
                break;
            }
        }
        
        return partition.multiply(factor).add(offset);
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
    
    private ByteSourceBitSource createBitSource() {
        byte[] hashBytes = hasher.hash(bytes);
        return new ByteSourceBitSource(hashBytes, additionalByteSourceFactory.apply(bytes, hashBytes));
    }

}
