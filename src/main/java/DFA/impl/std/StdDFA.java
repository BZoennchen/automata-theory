package DFA.impl.std;

import DFA.gen.GenDFA;

import java.util.Set;

/**
 * A standard implementation of the GenDFA.
 *
 * Input symbol identifier type: Character
 * State identifier type: Integer
 *
 * @author Benedikt Zoennchen
 */
public class StdDFA extends GenDFA<Character, Integer, StdDFASymbol, StdDFAState, StdDFARule> {

    public StdDFA(final Set<StdDFARule> rules, final StdDFAState initialStates, final Set<StdDFAState> finalStates) {
        super(rules, initialStates, finalStates);
    }

}
