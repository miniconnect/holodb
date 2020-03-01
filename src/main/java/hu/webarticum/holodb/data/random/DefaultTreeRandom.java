package hu.webarticum.holodb.data.random;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import hu.webarticum.holodb.util.ByteUtil;
import hu.webarticum.holodb.util.bitsource.BitSource;
import hu.webarticum.holodb.util.bitsource.JavaRandomByteSource;

public class DefaultTreeRandom implements TreeRandom {

    private static final int RANDOM_NUMBER_MAX_RETRIES = 15;
    
    private static final byte[] SEPARATOR = new byte[] { (byte) 0b11111111 };
    
    private static final DefaultTreeRandom ROOT_INSTANCE = new DefaultTreeRandom(null, new byte[2]);
    
    
    private final DefaultTreeRandom parent;
    
    private final byte[] bytes;
    
    private transient Mac macForChildren = null;
    

    public DefaultTreeRandom(long number) {
        this(BigInteger.valueOf(number));
    }
    
    public DefaultTreeRandom(BigInteger number) {
        this(number.toByteArray());
    }
    
    public DefaultTreeRandom(byte[] bytes) {
        this(ROOT_INSTANCE, bytes);
    }

    private DefaultTreeRandom(DefaultTreeRandom parent, byte[] bytes) {
        this.parent = parent;
        this.bytes = bytes;
    }
    

    @Override
    public DefaultTreeRandom sub(BigInteger number) {
        return sub(number.toByteArray());
    }
    
    @Override
    public DefaultTreeRandom sub(byte... bytes) {
        return new DefaultTreeRandom(this, bytes);
    }


    @Override
    public BigInteger getNumber(BigInteger highExclusive) {
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

    @Override
    public byte[] getBytes(int numberOfBytes) {
        return createBitSource().fetch(numberOfBytes * 8);   
    }

    private BitSource createBitSource() {
        byte[] macBytes = parent.getMacForChildren().doFinal(bytes);
        Random random = new Random(ByteUtil.firstBytesToLong(macBytes));
        return new BitSource(macBytes, new JavaRandomByteSource(random));
    }
    
    private Mac getMacForChildren() {
        try {
            return getMacForChildrenUnwrapped();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }
    
    private Mac getMacForChildrenUnwrapped() throws GeneralSecurityException {
        if (macForChildren == null) {
            Mac mac = Mac.getInstance("HmacSHA256");
            byte[] keyBytes = composeKey();
            SecretKeySpec key = new SecretKeySpec(keyBytes, "RawBytes");
            mac.init(key);
            macForChildren = mac;
        }
        return macForChildren;
    }
    
    private byte[] composeKey() {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        List<DefaultTreeRandom> path = new LinkedList<>();
        DefaultTreeRandom nextParent = this;
        while (nextParent != null) {
            path.add(0, nextParent);
            nextParent = nextParent.parent;
        }
        for (DefaultTreeRandom item : path) {
            byteStream.write(item.bytes, 0, item.bytes.length);
            byteStream.write(SEPARATOR, 0, SEPARATOR.length);
        }
        return byteStream.toByteArray();
    }
    
}
