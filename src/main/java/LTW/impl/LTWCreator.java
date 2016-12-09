package LTW.impl;

import LTW.inter.ILTWCreator;
import grammar.impl.SLP;
import symbol.StdState;
import symbol.StdStateFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * @author Benedikt Zoennchen
 */
public class LTWCreator implements ILTWCreator<Character, Character, Integer, RankedSymbol,StdState, LTWRule, LTW> {

    private StdStateFactory factory = new StdStateFactory();

    @Override
    public StdState createState(final Integer name) {
        return factory.createState(name);
    }

    @Override
    public StdState createFreshState() {
        return factory.createFreshState();
    }

    @Override
    public LTWRule createRule(
            final StdState srcState,
            final RankedSymbol symbol,
            final List<StdState> destState,
            final List<SLP<Character>> outputWords,
            final Function<Integer, Integer> permutation) {
        return new LTWRule(srcState, symbol, destState, outputWords, permutation);
    }

    @Override
    public LTW createLTW(final Set<LTWRule> rules, final Set<StdState> initialStates) {
        return new LTW(rules, initialStates);
    }

    @Override
    public LTW createLTW(Set<LTWRule> rules, StdState initialState) {
        Set<StdState> initialStates = new HashSet<>();
        initialStates.add(initialState);
        return createLTW(rules, initialStates);
    }
}
