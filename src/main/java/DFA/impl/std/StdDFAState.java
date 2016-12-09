package DFA.impl.std;

import symbol.GenNamedSymbol;

/**
 * A standard implementation of the GenNamedSymbol.
 *
 * State identifier type: Integer
 *
 * @author Benedikt Zoennchen
 */
public class StdDFAState extends GenNamedSymbol<Integer> {
    public StdDFAState(Integer element) {
        super(element);
    }
}
