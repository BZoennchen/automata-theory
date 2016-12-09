package N2W.impl.reduction;

import N2W.gen.GenN2WRule;
import LTW.impl.LTWRule;
import LTW.impl.RankedSymbol;
import grammar.impl.SLP;
import utils.Pair;

/**
 * Reduction implementation of a N2WRule that uses:
 *      + SLPs of type Character as an output word
 *      + States of the form (r, j), where r is of the LTWRule and j is an integer
 *      + reads an nested letter of type RankedSymbol (input symbol of an LTW)
 *      + stack symbols are equals to states.
 *
 * @author Benedikt Zoennchen
 */
public class N2WRule extends GenN2WRule<
        SLP<Character>, Pair<LTWRule, Integer>, RankedSymbol, Pair<LTWRule, Integer>,
        N2WState,
        NestedLetter,
        StackSymbol> {


    public N2WRule(final N2WState startState, final N2WState endState, final NestedLetter nestedWord, final SLP<Character> outputWord, final StackSymbol stackSymbol) {
        super(startState, endState, nestedWord, outputWord, stackSymbol);
    }
}
