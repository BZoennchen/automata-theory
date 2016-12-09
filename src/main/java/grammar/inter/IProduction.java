package grammar.inter;


import symbol.IJezSymbol;

/**
 * A grammar production of a context-free grammar i.e. A -> alpha such that alpha is a word over the alphabet of the grammar.
 *
 * @author Benedikt Zoennchen
 */
public interface IProduction<N, S extends IJezSymbol<N>, W extends IReferencedWord<N, S>> {

    /**
     * Returns the left-hand side of the production.
     * @return the left-hand side of the production
     */
    S getLeft();

    /**
     * Returns the right-hand side of the production.
     * @return the right-hand side of the production
     */
    W getRight();

    /**
     * Clones the production.
     * @return a clone (copy) of the production
     */
    IProduction<N, S, W> clone();

    /**
     * Returns the numbers of non-terminals on the right-hand side.
     * @return the numbers of non-terminals on the right-hand side
     */
    int getNumberOfNonTerminals();

    /**
     * Returns the numbers of terminals on the right-hand side.
     * @return the numbers of terminals on the right-hand side
     */
    int getNumberOfTerminals();

}
