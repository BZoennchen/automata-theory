package LTW.impl;

import LTW.gen.GenLTWRule;
import grammar.impl.SLP;
import symbol.StdState;

import java.util.List;
import java.util.function.Function;

/**
 * @author Benedikt Zoennchen
 */
public class LTWRule extends GenLTWRule<Character, Character, Integer, RankedSymbol, StdState> {
    public LTWRule(StdState srcState, RankedSymbol symbol, List<StdState> destStates, List<SLP<Character>> outputWords, Function<Integer, Integer> permutation) {
        super(srcState, symbol, destStates, outputWords, permutation);
    }
}
