package N2W.inter;

import symbol.Symbol;

/**
 * A nested letter i.e. (f, op) or (f, cl) such that f is a symbol.
 *
 * @author Benedikt Zoennchen
 *
 * @param <B> the type of the identifier of the symbol
 */
public interface INestedLetter<B> extends Symbol {

    /**
     * Returns the identifier of the symbol.
     *
     * @return the identifier of the symbol
     */
    B getElement();

    /**
     * Returns true if this symbol is an opening symbol i.e. (f, op).
     *
     * @return true if this symbol is an opening symbol
     */
    boolean isOpening();

    /**
     * Returns true if this symbol is an closing symbol i.e. (f, cl).
     *
     * @return true if this symbol is an closing symbol
     */
    boolean isClosing();
}
