package hu.webarticum.holodb.data.random;

import java.math.BigInteger;

import javax.crypto.Cipher;

// TODO: define a hidden implicit parent of root for storing root cipher (necessary?)
// TODO: ensure minimal key size
// TODO: use additional content as salt
// TODO: if requested byte size is even larger, pad with zeros
// TODO: create random byte source for additional bytes (when value is larger then the maximum BigInteger)
public class DefaultTreeRandom implements TreeRandom {

    private static final byte[] DEFAULT_BYTES = new byte[4];
    
    
    private final DefaultTreeRandom parent;
    
    private final byte[] bytes;
    
    private transient Cipher cipherForChildren = null;
    

    public DefaultTreeRandom() {
        this(DEFAULT_BYTES);
    }

    public DefaultTreeRandom(long number) {
        this(BigInteger.valueOf(number));
    }
    
    public DefaultTreeRandom(BigInteger number) {
        this(number.toByteArray());
    }
    
    public DefaultTreeRandom(byte[] bytes) {
        this(null, bytes);
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
        
        
        // TODO
        return null;
        
        
    }

    @Override
    public byte[] getBytes(int numberOfBytes) {
        
        
        // TODO
        return null;
        
        
    }
    
}
