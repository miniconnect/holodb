package hu.webarticum.holodb.simplemodel;

import java.math.BigInteger;
import java.util.List;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import hu.webarticum.holodb.core.data.source.Source;

public class AutomatonSource implements Source<String> {

    private final int size;

    private Automaton automaton;
    
    
    public AutomatonSource(String pattern, int size) {
        this.size = size;
        this.automaton = new RegExp(pattern).toAutomaton();
    }
    
    
    @Override
    public Class<String> type() {
        return String.class;
    }
    
    @Override
    public BigInteger size() {
        return BigInteger.valueOf(size);
    }

    @Override
    public String get(BigInteger index) {
        StringBuilder builder = new StringBuilder();
        generateNext(index.intValue(), builder, automaton.getInitialState());
        return builder.toString();
    }

    private void generateNext(int number, StringBuilder builder, State state) {
        List<Transition> transitions = state.getSortedTransitions(false);
        if (transitions.isEmpty()) {
            return;
        }
        
        boolean isAccept = state.isAccept();
        int count = transitions.size();
        int nextNumber = nextInt(number);
        
        int max = isAccept ? count + 1 : count;
        int branch = nextNumber % max;
        if (isAccept && branch == count) {
            return;
        }
        
        Transition transition = transitions.get(branch);
        
        nextNumber = nextInt(nextNumber);
        builder.append(chooseChar(nextNumber, transition));
        
        generateNext(nextInt(nextNumber), builder, transition.getDest());
    }
    
    private static int nextInt(int number) {
        return Math.abs((number + 1) * 37);
    }

    private static char chooseChar(int number, Transition transition) {
        int min = transition.getMin();
        int max = transition.getMax();
        int range = max - min + 1;
        int numVal = (number % range) + min;
        return (char) numVal;
    }
    
    
    
}
