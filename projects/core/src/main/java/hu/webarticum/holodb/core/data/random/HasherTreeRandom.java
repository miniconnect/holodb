package hu.webarticum.holodb.core.data.random;

import java.util.function.Function;

import hu.webarticum.holodb.core.data.bitsource.BitSource;
import hu.webarticum.holodb.core.data.bitsource.ByteSource;
import hu.webarticum.holodb.core.data.bitsource.ByteSourceBitSource;
import hu.webarticum.holodb.core.data.bitsource.FastByteSource;
import hu.webarticum.holodb.core.data.hasher.FastHasher;
import hu.webarticum.holodb.core.data.hasher.Hasher;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class HasherTreeRandom implements TreeRandom {
    
    private static final long DEFAULT_SEED = 0L;
    
    private static final int DEFAULT_HASH_LENGTH = 8;

    private static final int NUMBER_SAMPLLING_MAX_RETRIES = 50;

    private static final byte SEPARATOR_BYTE = (byte) 0b11111111;

    
    private final byte[] bytes;
    
    private final Hasher hasher;
    
    private final Function<byte[], ByteSource> additionalByteSourceFactory;
    

    public HasherTreeRandom() {
        this(DEFAULT_SEED);
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

    public HasherTreeRandom(long seed, Hasher hasher, Function<byte[], ByteSource> additionalByteSourceFactory) {
        this(LargeInteger.of(seed), hasher, additionalByteSourceFactory);
    }

    public HasherTreeRandom(LargeInteger seed, Hasher hasher, Function<byte[], ByteSource> additionalByteSourceFactory) {
        this(seed.toByteArray(), hasher, additionalByteSourceFactory);
    }

    public HasherTreeRandom(String seed, Hasher hasher, Function<byte[], ByteSource> additionalByteSourceFactory) {
        this(seed.getBytes(), hasher, additionalByteSourceFactory);
    }

    public HasherTreeRandom(byte[] seed, Hasher hasher, Function<byte[], ByteSource> additionalByteSourceFactory) {
        this.bytes = hasher.hash(seed);
        this.hasher = hasher;
        this.additionalByteSourceFactory = additionalByteSourceFactory;
    }

    private static Hasher createDefaultHasher(byte[] seed) {
        return new FastHasher(seed, DEFAULT_HASH_LENGTH);
    }

    private static Function<byte[], ByteSource> createDefaultAdditionalByteSourceFactory() {
        return bs -> new FastByteSource(bs[0]);
    }


    @Override
    public TreeRandom sub(byte[] key) {
        byte[] subSeed = new byte[key.length + 1 + bytes.length];
        System.arraycopy(key,  0, subSeed, 0, key.length);
        subSeed[key.length] = SEPARATOR_BYTE;
        System.arraycopy(bytes, 0, subSeed, key.length + 1, bytes.length);
        return new HasherTreeRandom(subSeed, hasher, additionalByteSourceFactory);
    }

    @Override
    public byte[] getBytes(int numberOfBytes) {
        if (numberOfBytes <= bytes.length) {
            byte[] result = new byte[numberOfBytes];
            System.arraycopy(bytes, 0, result, 0, numberOfBytes);
            return result;
        }
        
        return createBitSource().fetch(numberOfBytes * 8);
    }

    @Override
    public LargeInteger getNumber(LargeInteger highExclusive) {
        if (highExclusive.isNonPositive()) {
            throw new IllegalArgumentException("High value must be positive");
        }
        
        if (highExclusive.isFittingInLong()) {
            return getNumberLong(highExclusive);
        } else {
            return getNumberLarge_XXX(highExclusive);
        }
    }

    private LargeInteger getNumberLong(LargeInteger highExclusive) {
        int numberOfBits = highExclusive.ceilingLog2().intValue();
        BitSource bitSource = createBitSource();

        for (int i = 0; i < NUMBER_SAMPLLING_MAX_RETRIES; i++) {
            byte[] fetchedBytes = bitSource.fetch(numberOfBits);
            LargeInteger candidate = LargeInteger.ofUnsigned(fetchedBytes);
            if (candidate.isLessThan(highExclusive)) {
                return candidate;
            }
        }
        
        return highExclusive.decrement();
    }
    
    private LargeInteger getNumberLarge_XXX(LargeInteger highExclusive) {
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
        
        BitSource bitSource = createBitSource();
        
        LargeInteger partition = exponentOfTwo > 0 ?
                LargeInteger.ofUnsigned(bitSource.fetch(exponentOfTwo)) :
                LargeInteger.ZERO;
        LargeInteger offset = LargeInteger.ZERO;
        for (int i = 0; i < NUMBER_SAMPLLING_MAX_RETRIES; i++) {
            LargeInteger offsetCandidate = LargeInteger.of(bitSource.fetch(bitCountOfFactor));
            if (offsetCandidate.compareTo(factor) < 0) {
                offset = offsetCandidate;
                break;
            }
        }
        
        return partition.multiply(factor).add(offset);
    }


    private BitSource createBitSource() {
        return new ByteSourceBitSource(bytes, () -> additionalByteSourceFactory.apply(bytes));
    }

}
