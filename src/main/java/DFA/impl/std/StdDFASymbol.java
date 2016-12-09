package DFA.impl.std;

import symbol.GenNamedSymbol;

/**
 * A standard implementation of the GenNamedSymbol.
 *
 * Input symbol identifier type: Character
 *
 * @author Benedikt Zoennchen
 */
public class StdDFASymbol extends GenNamedSymbol<Character> {
    public StdDFASymbol(final Character element) {
        super(element);
    }
}
