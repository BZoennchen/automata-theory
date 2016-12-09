package N2W.gen;

import symbol.GenNamedSymbol;

/**
 * The generic immutable implementation of a stack symbol of the N2W.
 *
 * @author Benedikt Zoennchen
 *
 * @param <C> the type of the identifier of the stack symbol
 */
public class GenStackSymbol<C> extends GenNamedSymbol<C> {
    public GenStackSymbol(C element) {
        super(element);
    }
}
