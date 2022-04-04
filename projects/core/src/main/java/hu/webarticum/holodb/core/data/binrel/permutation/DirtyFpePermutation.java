package hu.webarticum.holodb.core.data.binrel.permutation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import hu.webarticum.holodb.core.data.random.TreeRandom;

/**
 * Generic Z_n FPE encryption, FE1 scheme
 * 
 * Software derived from a New-BSD licensed implementation for .NET http://dotfpe.codeplex.com
 * That in turn was ported from the Botan library http://botan.randombit.net/fpe.html.
 * Using the scheme FE1 from the paper "Format-Preserving Encryption" by Bellare, Rogaway, et al. (http://eprint.iacr.org/2009/251)
 *
 * @author Rob Shepherd
 * @author David Horvath
 */
public class DirtyFpePermutation implements Permutation {
    
    private static final BigInteger MAX_PRIME = BigInteger.valueOf(65535);

    /**
     * According to a paper by Rogaway, Bellare, etc, the min safe number
     * of rounds to use for FPE is 2+log_a(b). If a >= b then log_a(b) &lt;= 1
     * so 3 rounds is safe. The FPE factorization routine should always
     * return a >= b, so just confirm that and return 3.
     */
    private static final int ROUNDS = 3;
    
    
    private Mac mac;

    private byte[] macPrefixBytes;
    
    private final BigInteger size;
    
    private final BigInteger a;
    
    private final BigInteger b;


    public DirtyFpePermutation(TreeRandom treeRandom, BigInteger size) {
        this.size = size;
        
        byte[] key = treeRandom.getBytes(16);
        mac = createMacInstance(key);
        
        byte[] sizeBytes = size.toByteArray();

        ByteArrayOutputStream macByteStream = new ByteArrayOutputStream();

        macByteStream.write(sizeBytes.length);
        macByteStream.write(sizeBytes, 0, sizeBytes.length);

        mac.reset();
        macPrefixBytes = mac.doFinal(macByteStream.toByteArray());
        
        BigInteger[] aAndB = factor(size);
        a = aAndB[0];
        b = aAndB[1];
    }
    

    private static Mac createMacInstance(byte[] key) {
        try {
            return createMacInstanceUnwrapped(key);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }
    
    private static Mac createMacInstanceUnwrapped(byte[] key) throws GeneralSecurityException {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key, "HmacSHA256");
        mac.init(secretKey);
        return mac;
    }
    
    
    @Override
    public BigInteger size() {
        return size;
    }
    
    @Override
    public BigInteger at(BigInteger index) {
        BigInteger result = index;
        for (int i = 0; i != ROUNDS; i++) {
            result = runEncryptionRound(result, i);
        }
        return result;
    }
    
    private BigInteger runEncryptionRound(BigInteger value, int round) {
        try {
            return runEncryptionRoundUnwrapped(value, round);
        } catch (IOException e) {
            throw new IllegalStateException("Encryption failed");
        }
    }

    private BigInteger runEncryptionRoundUnwrapped(BigInteger value, int round) throws IOException {
        BigInteger k = value.divide(b);
        BigInteger r = value.mod(b);
        BigInteger encrypted;
        encrypted = runMac(round, r);
        BigInteger w = (k.add(encrypted)).mod(a);
        return a.multiply(r).add(w);
    }

    @Override
    public BigInteger indexOf(BigInteger value) {
        BigInteger result = value;
        for (int i = 0; i != ROUNDS; i++) {
            result = runDecryptionRound(result, ROUNDS - i - 1);
        }
        return result;
    }

    private BigInteger runDecryptionRound(BigInteger value, int round) {
        try {
            return runDecryptionRoundUnwrapped(value, round);
        } catch (IOException e) {
            throw new IllegalStateException("Decryption failed");
        }
    }
    
    private BigInteger runDecryptionRoundUnwrapped(BigInteger value, int round) throws IOException {
        BigInteger w = value.mod(a);
        BigInteger r = value.divide(a);
        BigInteger encrypted;
        encrypted = runMac(round, r);
        BigInteger bigInteger = (w.subtract(encrypted));
        BigInteger k = bigInteger.mod(a);
        return b.multiply(k).add(r);
    }

    public BigInteger runMac(int roundNo, BigInteger value) throws IOException {
        byte[] valueBytes = value.toByteArray();
        ByteArrayOutputStream macByteStream = new ByteArrayOutputStream();
        macByteStream.write(macPrefixBytes);
        macByteStream.write(roundNo);
        macByteStream.write(valueBytes.length);
        macByteStream.write(valueBytes);

        mac.reset();
        byte[] decryptedBytes = mac.doFinal(macByteStream.toByteArray());
        byte[] nullPrefixedBytes = new byte[decryptedBytes.length + 1];
        System.arraycopy(decryptedBytes, 0, nullPrefixedBytes, 1, decryptedBytes.length);
        
        return new BigInteger(nullPrefixedBytes);
    }
    

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    ////////////////// Helpers... //////////////////
    
    /**
     * Factor n into a and b which are as close together as possible.
     * Assumes n is composed mostly of small factors which is the case for
     * typical uses of FPE (typically, n is a power of 10)
     * Want a >= b since the safe number of rounds is 2+log_a(b);
     * if a >= b then this is always 3
     */
    private static BigInteger[] factor(BigInteger n) {
        if (n.equals(BigInteger.ZERO)) {
            return new BigInteger[] { BigInteger.ZERO, BigInteger.ZERO };
        }
        
        BigInteger a = BigInteger.ONE;
        BigInteger b = BigInteger.ONE;
           
        int nLowZero = lowZeroBits(n);
        a = a.shiftLeft(nLowZero / 2);
        b = b.shiftLeft(nLowZero - (nLowZero / 2) );
            
        n = n.shiftRight(nLowZero);

        BigInteger prime = BigInteger.ONE;
        while(prime.compareTo(MAX_PRIME) <= 0) {
            prime = prime.nextProbablePrime();
            while (n.mod(prime).compareTo(BigInteger.ZERO) == 0) {
                a = a.multiply(prime);
                if (a.compareTo(b) > 0) {
                    BigInteger oldB = b;
                    b = a;
                    a = oldB;
                }
                n = n.divide(prime);
            }
            if (a.compareTo(BigInteger.ONE) > 0 && b.compareTo(BigInteger.ONE) > 0) {
                break;
            }
        }

        if (a.compareTo(b) > 0) {
            BigInteger oldB = b;
            b = a;
            a = oldB;
        }
        a = a.multiply(n);
        if (a.compareTo(b) < 0) {
            BigInteger oldB = b;
            b = a;
            a = oldB;
        }

        if (a.compareTo(BigInteger.ONE) < 0 || b.compareTo(BigInteger.ONE) < 0) {
            throw new IllegalArgumentException("Could not factor n for use in FPE");
        }
        
        return new BigInteger[] { a, b };
    }
    
    private static int lowZeroBits(BigInteger n) {
        int lowZero = 0;
        if (n.signum() > 0) {
            byte[] bytes = n.toByteArray();
            for (int i = bytes.length - 1; i >= 0; i--) {
                int x =  (bytes[i] & 0xFF);
                if (x > 0) {
                    lowZero += countTrailingZeroes((byte) x);
                    break;
                } else {
                    lowZero += 8;
                }
            }
        }
        return lowZero;
    }

    private static int countTrailingZeroes(byte n) {
        for (int i = 0; i < 8; i++) {
            if (((n >> i) & 0x01) > 0) {
                return i;
            }
        }
        return 8;
    }
    
}
