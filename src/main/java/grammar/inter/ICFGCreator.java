package grammar.inter;

import symbol.IJezSymbol;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The ICFGCreator is a factory for the creation of grammars. Some operations require to introduce fresh non-terminals.
 * Such non-terminals has to be distinct from all non-terminals used ba a grammar. Therefore operations require often to
 * use the creator that creates a certain grammar! Otherwise we would have to initialize a new creator and add all
 * non-terminals of the grammars beforehand which costs is running time which we want to avoid.
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
public interface ICFGCreator<
        N,
        S extends IJezSymbol<N>,
        W extends IReferencedWord<N, S>,
        P extends IProduction<N, S, W>,
        C extends ICFG<N, S, W, P>,
        Z extends ISLP<N, S, W, P>> {

    /**
     * Creates a CFG defined by the productions and the axiom
     *
     * @param productions   the set of productions of the CFG
     * @param axiom         the single axiom of the CFG
     * @return a CFG
     */
    C createCFG(final Set<P> productions, final S axiom);

    /**
     * Creates a CFG defined by the productions and the axioms
     *
     * @param productions   the set of productions of the CFG
     * @param axioms        the set of axioms
     * @return a CFG
     */
    C createCFG(final Set<P> productions, final Set<S> axioms);

    /**
     * Replaces each non-terminal of a CFG by a fresh non-terminal. This is useful if we have different grammars
     * and we want that they do not share a common non-terminal.
     *
     * @param cfg                   the base CFG
     * @param replacementMap        a mapping X -> Y, where X was replaced by Y
     * @return a CFG
     */
    C freshNonTerminals(C cfg, final Map<S, S> replacementMap);

    /**
     * Returns a copy of the CFG.
     *
     * @param cfg the base CFG that we copy
     * @return a copy of the CFG
     */
    C copy(C cfg);

    /**
     * Replaces each non-terminal of a CFG by a new non-terminal starting from the non-terminal with id = 0. This is useful if
     * want that non-terminals are in an interval [|N|] where N is the set of non-terminals.
     *
     * @param cfg the base CFG
     * @return a CFG with renamed non-terminals
     */
    C resetNonTerminals(C cfg);

    /**
     * Creates a SLP defined by the productions and the axiom representing a single word.
     *
     * @param productions   the set of productions of the CFG
     * @param axiom         a single axiom
     * @return a SLP
     */
    Z createSLP(final Map<S, P> productions, final S axiom);

    /**
     * Creates a SLP defined by the productions and the axioms
     *
     * @param productions   a map of productions of the CFG
     * @param axioms        the set of axioms
     * @return a SLP
     */
    Z createSLP(final Map<S, P> productions, final Set<S> axioms);

    /**
     * Creates a SLP defined by the productions and the axioms
     *
     * @param productions   set of productions of the CFG
     * @param axioms        the set of axioms
     * @return a SLP
     */
    Z createSLP(final Set<P> productions, final Set<S> axioms);

    /**
     * Returns a SLP representing the empty word.
     * @return a SLP representing the empty word
     */
    Z emptyWord();

    /**
     * Creates a SLP containing only one production with the right-hand side equal to symbols.
     *
     * @param symbol the right-hand side of the production
     * @return a SLP containing only one production
     */
    Z oneProduction(N ... symbol);

    /**
     * Creates a SLP containing only one production with the right-hand side equal to symbols.
     *
     * @param symbol the right-hand side of the production
     * @return a SLP containing only one production
     */
    Z oneProduction(List<N> symbol);

    /**
     * Replaces each non-terminal of a SLP by a fresh non-terminal. This is useful if we have different grammars
     * and we want that they do not share a common non-terminal.
     *
     * @param slp                   the base CFG
     * @param replacementMap        a mapping X -> Y, where X was replaced by Y
     * @return a CFG
     */
    Z freshNonTerminals(final Z slp, final Map<S, S> replacementMap);

    /**
     * Returns a copy of the SLP.
     *
     * @param slp the base CFG that we copy
     * @return a copy of the SLP
     */
    Z copy(Z slp);

    /**
     * Replaces each non-terminal of a SLP by a new non-terminal starting from the non-terminal with id = 0. This is useful if
     * want that non-terminals are in an interval [|N|] where N is the set of non-terminals.
     *
     * @param slp the base SLP
     * @return a SLP with renamed non-terminals
     */
    Z resetNonTerminals(Z slp);

    /**
     * Creates a grammar production.
     *
     * @param left  the left-hand side of the production
     * @param right the right-hand side of the production
     * @return a new grammar production
     */
    P createProduction(final S left, W right);

    /**
     * Creates a grammar production.
     *
     * @param left  the left-hand side of the production
     * @param right the right-hand side of the production
     * @return a new grammar production
     */
    P createProduction(final S left, S ... right);

    /**
     * Creates a word i.e. a right-hand side of a grammar production.
     *
     * @param symbols list of symbols of the right-hand side
     * @return a word i.e. a right-hand side of a grammar production
     */
    W createWord(final List<S> symbols);

    /**
     * Create and returns a fresh unused non-terminal. Note this only holds for grammar
     * created by this CFGCreator.
     *
     * @return a fresh unused non-terminal
     */
    S createFreshNonTerminal();

    /**
     * A fresh (if the symbol is not in use jet) or a used symbol identified by its name.
     *
     * @param name      the identifier of the symbol i.e. its name
     * @param terminal  true => loopup for a terminal, otherwise lookup for a non-terminal
     *
     * @return resh (if the symbol is not in use jet) or a used symbol
     */
    S lookupSymbol(N name, boolean terminal);

    /**
     * Returns the IJezSymbolFactory of this creator. This factory contains the base non-termial and
     * terminal alphabet.
     *
     * @return the IJezSymbolFactory of this creator
     */
    IJezSymbolFactory<N, S, W, P> getSymbolFactory();

    /**
     * Copies a set of productions.
     *
     * @param productions the base productions
     * @return a set containing copies of the base productions
     */
    Set<P> copyProductions(final Set<P> productions);

    /**
     * Copies a set of productions.
     *
     * @param productions the base productions
     * @return a map containing copies of the base productions
     */
    Map<S, P> copyProductions(final Map<S, P> productions);
}
