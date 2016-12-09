package DFA.impl.std;

import DFA.gen.GenDFARule;

/**
 * A standard implementation of the GenDFARule.
 *
 * Input symbol identifier type: Character
 * State identifier type: Integer
 *
 * @author Benedikt Zoennchen
 */
public class StdDFARule extends GenDFARule<Character, Integer, StdDFASymbol, StdDFAState> {

    public StdDFARule(final StdDFAState srcState, final StdDFAState destState, final StdDFASymbol symbol) {
        super(srcState, destState, symbol);
    }

}
