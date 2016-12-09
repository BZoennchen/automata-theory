package N2W.impl.std;

import N2W.gen.GenN2WRule;
import symbol.StdState;

import java.util.List;

/**
 * Standard implementation of a GenN2WRule. The output word is a list of nested words, states identified by Integers, nested words are identified by Characters,
 * stack symbols are identified by Integers.
 *
 * @author Benedikt Zoennchen
 */
public class StdN2WRule extends GenN2WRule<List<StdNestedWord>, Integer, Character, Integer, StdState, StdNestedWord, StdStackSymbol> {
    public StdN2WRule(StdState startState, StdState endState, StdNestedWord nestedWord, List<StdNestedWord> outputWord, StdStackSymbol stackSymbol) {
        super(startState, endState, nestedWord, outputWord, stackSymbol);
    }
}
