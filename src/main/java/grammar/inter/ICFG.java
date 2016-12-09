package grammar.inter;

import symbol.IJezSymbol;

import java.util.Set;
import java.util.stream.Stream;

/**
 * A context-free language defined by its productions and axioms.
 *
 * @author Benedikt Zoennchen
 *
 * @param <N>   the type of the identifier of terminal and non-terminal symbols of the grammar
 * @param <S>   the type of the terminal and non-terminal symbols of the grammar
 * @param <W>   the type of the right-hand side of the grammar
 * @param <P>   the type of the grammar production
 */
public interface ICFG<N, S extends IJezSymbol<? extends N>, W extends IReferencedWord<? extends N, S>, P extends IProduction<? extends N, S, W>> {

    /**
     * Returns the productions of the CFG.
     *
     * @return the productions of the CFG.
     */
    Set<P> getProductions();

    /**
     * Returns the set of all productions P of the CFG such P = {p in productions | lhs(p) = symbol}.
     *
     * @param symbol a non-terminal
     * @return the set of all productions P of the CFG such P = {p in productions | lhs(p) = symbol}.
     */
    Set<P> getProductions(final S symbol);

    /**
     * Returns the set of non-terminals of this CFG.
     *
     * @return the set of non-terminals of this CFG
     */
    Set<S> getNonTerminals();

    /**
     * Returns a stream of non-terminals of this CFG.
     *
     * @return a stream of non-terminals of this CFG
     */
    Stream<S> getNonTerminalStream();

    /**
     * Returns a stream of terminals of this CFG.
     *
     * @return a stream of terminals of this CFG
     */
    Stream<S> getTerminalStream();

    /**
     * Returns the set of terminals of this CFG.
     *
     * @return the set of terminals of this CFG
     */
    Set<S> getTerminals();

    /**
     * Returns the set of axioms of this CFG.
     *
     * @return the set of axioms of this CFG
     */
    Set<S> getAxioms();

    /**
     * Returns an arbitrary axiom of this CFG.
     *
     * @return an arbitrary axiom of this CFG
     */
    S getAxiom();

    /**
     * Clones this CFG.
     *
     * @return a clone (copy) of this CFG
     */
    ICFG<N, S, W, P> clone();
}
