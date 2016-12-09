package LTW.inter;

import symbol.Symbol;

/**
 * A ranked symbol i.e. a symbol of the input alphabet of a LTW.
 *
 * @author Benedikt Zoennchen
 *
 * @param <A> the type of the identifier of the ranked symbol
 */
public interface IRankedSymbol<A> extends Symbol {

    /**
     * Returns the arity (the rank) of the ranked symbol.
     *
     * @return the rank of the symbol
     */
    int getArity();

    /**
     * Returns the identifier of the ranked symbol.
     *
     * @return the identifier of the symbol
     */
    A getElement();
}
