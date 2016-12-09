package N2W.impl.reduction;

import LTW.impl.LTWRule;
import LTW.impl.RankedSymbol;
import N2W.gen.GenN2W;
import grammar.impl.SLP;
import utils.Pair;

import java.util.Set;

/**
 * Reduction implementation of a N2W that uses:
 *      + SLPs of type Character as an output word
 *      + States of the form (r, j), where r is of the LTWRule and j is an integer
 *      + reads an nested letter of type RankedSymbol (input symbol of an LTW)
 *      + stack symbols are equals to states.
 *
 * @author Benedikt Zoennchen
 */
public class N2W extends GenN2W<SLP<Character>, Pair<LTWRule, Integer>, RankedSymbol, Pair<LTWRule, Integer>, N2WState, NestedLetter, StackSymbol, N2WRule> {
    public N2W(Set<N2WRule> rules, Set<N2WState> initialStates, Set<N2WState> finalStates) {
        super(rules, initialStates, finalStates);
    }
}
