package hu.webarticum.holodb.data.random;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

// TODO: parent reference only?
// TODO: separated head bytes?
// TODO: key from the entire head head bytes?

public class DefaultTreeRandomOld implements TreeRandom {

    private final byte[] keyBytes;

    private final byte[] valueBytes;
    
    private final Cipher cipher;
    
    
    public DefaultTreeRandomOld() {
        this(new byte[0], new byte[0]);
    }

    private DefaultTreeRandomOld(byte[] keyBytes, byte[] valueBytes) {
        this.keyBytes = keyBytes;
        this.valueBytes = valueBytes;

        try {
            this.cipher = createCipher(valueBytes);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Can not create cipher", e); // FIXME exception type?
        }
    }
    
    private static Cipher createCipher(byte[] bytes) throws GeneralSecurityException {
        int keySize = 16;
        byte[] key = new byte[keySize];
        int copyLength = Math.min(keySize, bytes.length);
        if (copyLength > 0) {
            System.arraycopy(bytes, 0, key, 0, copyLength);
        }
        
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");

        Cipher cipher = Cipher.getInstance("AES/CTR/PKCS5PADDING");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(new byte[16]));
        
        return cipher;
    }
    

    @Override
    public DefaultTreeRandomOld sub(BigInteger number) {
        return sub(number.toByteArray());
    }

    @Override
    public DefaultTreeRandomOld sub(byte... bytes) {
        int defaultKeySize = this.keyBytes.length + bytes.length + 1;
        ByteArrayOutputStream newKeyBytes = new ByteArrayOutputStream(defaultKeySize);
        newKeyBytes.write(this.keyBytes, 0, this.keyBytes.length);
        for (byte b : bytes) {
            if (b == Byte.MAX_VALUE) {
                newKeyBytes.write((byte) 1);
                newKeyBytes.write((byte) 0);
            } else {
                newKeyBytes.write(b);
            }
        }
        newKeyBytes.write(Byte.MAX_VALUE);

        int valueSize = this.valueBytes.length + bytes.length;
        ByteArrayOutputStream newValueBytes = new ByteArrayOutputStream(valueSize);
        newValueBytes.write(this.valueBytes, 0, this.valueBytes.length);
        newValueBytes.write(bytes, 0, bytes.length);
        return new DefaultTreeRandomOld(newKeyBytes.toByteArray(), newValueBytes.toByteArray());
    }

    @Override
    public byte getByte() {
        return getBytes(1)[0];
    }

    @Override
    public BigInteger getNumber(BigInteger highExclusive) {
        if (highExclusive.signum() != 1) {
            throw new IllegalArgumentException("High value must be positive");
        }
        
        BigInteger two = BigInteger.valueOf(2);
        int exponentOfTwo = 0;
        BigInteger factor = highExclusive;
        while (factor.mod(two).equals(BigInteger.ZERO)) {
            factor = factor.divide(two);
            exponentOfTwo++;
        }
        int factorBitLength = factor.bitLength();
        int initialBitLength = exponentOfTwo + factorBitLength;
        //BitSource bitSource = new BitSource(initialBuffer, byteSupplier) // reverse order...
        System.out.println("initialBitLength: " + initialBitLength);
        
        // TODO
        return BigInteger.ZERO;
        
    }

    @Override
    public byte[] getBytes(int numberOfBytes) {
        if (numberOfBytes == 0) {
            return new byte[0]; // XXX
        }
        
        byte[] inputBytes = new byte[numberOfBytes];
        int valueLength = Math.min(numberOfBytes, valueBytes.length);
        if (valueLength > 0) {
            System.arraycopy(valueBytes, 0, inputBytes, 0, valueLength);
        }
        
        try {
            return cipher.doFinal(inputBytes);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("oops", e);
        }
                
    }
    
}
