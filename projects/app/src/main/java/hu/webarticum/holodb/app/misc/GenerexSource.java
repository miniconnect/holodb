package hu.webarticum.holodb.app.misc;

import java.math.BigInteger;
import java.util.Optional;

import com.mifmif.common.regex.Generex;

import hu.webarticum.holodb.core.data.random.TreeRandom;
import hu.webarticum.holodb.core.data.source.Source;
import hu.webarticum.miniconnect.lang.ImmutableList;

public class GenerexSource implements Source<String> {
    
    private final Generex generex;
    
    private final TreeRandom treeRandom;
    
    private final BigInteger size;
    

    public GenerexSource(Generex generex, TreeRandom treeRandom, BigInteger size) {
        this.generex = generex;
        this.treeRandom = treeRandom;
        this.size = size;
    }
    

    @Override
    public Class<?> type() {
        return String.class;
    }

    @Override
    public BigInteger size() {
        return size;
    }

    @Override
    public String get(BigInteger index) {
        int seed = treeRandom.sub(index).getNumber(BigInteger.valueOf(Integer.MAX_VALUE)).intValue();
        generex.setSeed(seed);
        return generex.random();
    }

    @Override
    public Optional<ImmutableList<String>> possibleValues() {
        return Optional.empty();
    }

}
