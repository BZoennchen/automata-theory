package grammar.inter;

import symbol.IJezSymbol;

import java.util.Collection;

/**
 * A factory that creates new CFGCreators that represents also factories for the creation of grammars.
 *
 * @author Benedikt Zoennchen
 *
 * @param <N>   the type of the identifier of terminal and non-terminal symbols of the grammar
 * @param <S>   the type of the terminal and non-terminal symbols of the grammar
 * @param <W>   the type of the right-hand side of the grammar
 * @param <P>   the type of the grammar production
 * @param <C>   the type of the CFG
 * @param <Z>   the type of the SLP
 */
public interface ICFGCreatorFactory<N,
        S extends IJezSymbol<N>,
        W extends IReferencedWord<N, S>,
        P extends IProduction<N, S, W>,
        C extends ICFG<N, S, W, P>,
        Z extends ISLP<N, S, W, P>> {

    /**
     * Creates a fresh CFGCreator.
     *
     * @return a fresh CFGCreator
     */
    ICFGCreator<N, S, W, P, C, Z> create();

    /**
     * a CFGCreator initialized by all non-terminals of the CFGs contained in the collection.
     *
     * @param cfgs a collection of CFGs
     * @return a CFGCreator
     */
    ICFGCreator<N, S, W, P, C, Z> create(final Collection<? extends ICFG<N, ?, ?, ?>> cfgs);

    /**
     * a CFGCreator initialized by all non-terminals of the CFGs contained in the array.
     *
     * @param cfgs a array of CFGs
     * @return a CFGCreator
     */
    ICFGCreator<N, S, W, P, C, Z> create(final ICFG<N, ?, ?, ?>... cfgs);

}
