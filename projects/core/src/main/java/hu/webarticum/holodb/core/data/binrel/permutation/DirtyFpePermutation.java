package hu.webarticum.holodb.core.data.binrel.permutation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import hu.webarticum.holodb.core.data.random.TreeRandom;
import hu.webarticum.miniconnect.lang.LargeInteger;

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

    private static final int DEFAULT_ROUNDS = 6;

    private static final LargeInteger MAX_PRIME = LargeInteger.of(65535L);


    private final LargeInteger size;

    private final int rounds;

    private Mac mac;

    private byte[] macPrefixBytes;

    private final LargeInteger a;

    private final LargeInteger b;


    public DirtyFpePermutation(TreeRandom treeRandom, LargeInteger size) {
        this(treeRandom, size, DEFAULT_ROUNDS);
    }

    public DirtyFpePermutation(TreeRandom treeRandom, LargeInteger size, int rounds) {
        this.rounds = rounds;
        this.size = size;

        byte[] key = treeRandom.getBytes(16);
        mac = createMacInstance(key);

        byte[] sizeBytes = size.toByteArray();

        ByteArrayOutputStream macByteStream = new ByteArrayOutputStream();

        macByteStream.write(sizeBytes.length);
        macByteStream.write(sizeBytes, 0, sizeBytes.length);

        mac.reset();
        macPrefixBytes = mac.doFinal(macByteStream.toByteArray());

        LargeInteger[] aAndB = factor(size);
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
    public LargeInteger size() {
        return size;
    }

    @Override
    public LargeInteger at(LargeInteger index) {
        LargeInteger result = index;
        for (int i = 0; i != rounds; i++) {
            result = runEncryptionRound(result, i);
        }
        return result;
    }

    private LargeInteger runEncryptionRound(LargeInteger value, int round) {
        try {
            return runEncryptionRoundUnwrapped(value, round);
        } catch (IOException e) {
            throw new IllegalStateException("Encryption failed");
        }
    }

    private LargeInteger runEncryptionRoundUnwrapped(LargeInteger value, int round) throws IOException {
        LargeInteger k = value.divide(b);
        LargeInteger r = value.mod(b);
        LargeInteger encrypted;
        encrypted = runMac(round, r);
        LargeInteger w = (k.add(encrypted)).mod(a);
        return a.multiply(r).add(w);
    }

    @Override
    public LargeInteger indexOf(LargeInteger value) {
        LargeInteger result = value;
        for (int i = 0; i != rounds; i++) {
            result = runDecryptionRound(result, rounds - i - 1);
        }
        return result;
    }

    private LargeInteger runDecryptionRound(LargeInteger value, int round) {
        try {
            return runDecryptionRoundUnwrapped(value, round);
        } catch (IOException e) {
            throw new IllegalStateException("Decryption failed");
        }
    }

    private LargeInteger runDecryptionRoundUnwrapped(LargeInteger value, int round) throws IOException {
        LargeInteger w = value.mod(a);
        LargeInteger r = value.divide(a);
        LargeInteger encrypted;
        encrypted = runMac(round, r);
        LargeInteger bigInteger = (w.subtract(encrypted));
        LargeInteger k = bigInteger.mod(a);
        return b.multiply(k).add(r);
    }

    public LargeInteger runMac(int roundNo, LargeInteger value) throws IOException {
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

        return LargeInteger.of(nullPrefixedBytes);
    }



















    ////////////////// Helpers... //////////////////

    /**
     * Factor n into a and b which are as close together as possible.
     * Assumes n is composed mostly of small factors which is the case for
     * typical uses of FPE (typically, n is a power of 10)
     * Want a >= b since the safe number of rounds is 2+log_a(b);
     * if a >= b then this is always 3
     */
    private static LargeInteger[] factor(LargeInteger n) {
        if (n.equals(LargeInteger.ZERO)) {
            return new LargeInteger[] { LargeInteger.ZERO, LargeInteger.ZERO };
        }

        LargeInteger a = LargeInteger.ONE;
        LargeInteger b = LargeInteger.ONE;

        int nLowZero = lowZeroBits(n);
        a = a.shiftLeft(nLowZero / 2);
        b = b.shiftLeft(nLowZero - (nLowZero / 2) );

        n = n.shiftRight(nLowZero);

        LargeInteger prime = LargeInteger.ONE;
        while(prime.compareTo(MAX_PRIME) <= 0) {
            prime = prime.nextProbablePrime();
            while (n.mod(prime).isZero()) {
                a = a.multiply(prime);
                if (a.compareTo(b) > 0) {
                    LargeInteger oldB = b;
                    b = a;
                    a = oldB;
                }
                n = n.divide(prime);
            }
            if (a.isGreaterThan(LargeInteger.ONE) && b.isGreaterThan(LargeInteger.ONE)) {
                break;
            }
        }

        if (a.compareTo(b) > 0) {
            LargeInteger oldB = b;
            b = a;
            a = oldB;
        }
        a = a.multiply(n);
        if (a.compareTo(b) < 0) {
            LargeInteger oldB = b;
            b = a;
            a = oldB;
        }

        if (a.isLessThan(LargeInteger.ONE) || b.isLessThan(LargeInteger.ONE)) {
            throw new IllegalArgumentException("Could not factor n for use in FPE");
        }

        return new LargeInteger[] { a, b };
    }

    private static int lowZeroBits(LargeInteger n) {
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
