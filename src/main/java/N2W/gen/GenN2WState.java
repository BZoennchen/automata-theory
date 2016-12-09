package N2W.gen;

import symbol.GenNamedSymbol;

/**
 * The generic immutable implementation of a N2W-state.
 *
 * @author Benedikt Zoennchen
 *
 * @param <A> the type of the identifier of the state of the N2W
 */
public class GenN2WState<A> extends GenNamedSymbol<A> {
    public GenN2WState(final A element) {
        super(element);
    }
}
