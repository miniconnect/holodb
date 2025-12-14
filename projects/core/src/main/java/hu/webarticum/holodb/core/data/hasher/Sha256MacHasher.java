package hu.webarticum.holodb.core.data.hasher;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import hu.webarticum.miniconnect.lang.LargeInteger;

public class Sha256MacHasher implements Hasher {

    private static final byte DEFAULT_BYTE = (byte) 0;


    private final Mac mac;


    public Sha256MacHasher() {
        this(new byte[] { DEFAULT_BYTE });
    }

    public Sha256MacHasher(String key) {
        this(key.getBytes(StandardCharsets.UTF_8));
    }

    public Sha256MacHasher(long key) {
        this(LargeInteger.of(key));
    }

    public Sha256MacHasher(LargeInteger key) {
        this(key.toByteArray());
    }

    public Sha256MacHasher(byte[] key) {
        mac = buildMac(key);
    }

    private static Mac buildMac(byte[] key) {
        try {
            return buildMacThrows(key);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            // never occurs
            throw new RuntimeException(e); // NOSONAR
        }
    }

    private static Mac buildMacThrows(byte[] key) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(normalizeKey(key), "RawBytes"));
        return mac;
    }

    private static byte[] normalizeKey(byte[] key) {
        return key.length == 0 ? new byte[] { DEFAULT_BYTE } : key;
    }


    @Override
    public byte[] hash(byte[] input) {
        return mac.doFinal(input);
    }

}
