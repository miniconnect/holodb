package hu.webarticum.holodb.bootstrap.misc;

import java.util.Optional;

import com.mifmif.common.regex.Generex;

import hu.webarticum.holodb.core.data.random.TreeRandom;
import hu.webarticum.holodb.core.data.source.Source;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class GenerexSource implements Source<String> {

    private final Generex generex;

    private final TreeRandom treeRandom;

    private final LargeInteger size;


    public GenerexSource(Generex generex, TreeRandom treeRandom, LargeInteger size) {
        this.generex = generex;
        this.treeRandom = treeRandom;
        this.size = size;
    }


    @Override
    public Class<?> type() {
        return String.class;
    }

    @Override
    public LargeInteger size() {
        return size;
    }

    @Override
    public String get(LargeInteger index) {
        int seed = treeRandom.sub(index).getNumber(LargeInteger.of(Integer.MAX_VALUE)).intValue();
        generex.setSeed(seed);
        return generex.random();
    }

    @Override
    public Optional<ImmutableList<String>> possibleValues() {
        return Optional.empty();
    }

}
