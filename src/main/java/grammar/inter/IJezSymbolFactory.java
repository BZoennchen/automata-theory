package grammar.inter;


import symbol.IJezSymbol;
import utils.Block;
import utils.Pair;

import java.util.List;

/**
 * A IJezSymbolFactory creates new symbols representing compressed symbols contained in its phase alphabet.
 * The second task is to manage a non-terminal alphabet and a base alphabet for the creation of grammars.
 *
 * All createTerminal-methods creates a symbols of the phase alphabet.
 *
 * @author Benedikt Zoennchen
 *
 * @param <N>   the type of the identifier of terminal and non-terminal symbols of the grammar
 * @param <S>   the type of the terminal and non-terminal symbols of the grammar
 * @param <W>   the type of the right-hand side of the grammar
 * @param <P>   the type of the grammar production
 */
public interface IJezSymbolFactory<N, S extends IJezSymbol<N>, W extends IReferencedWord<N, S>, P extends IProduction<N, S, W>> {

    /**
     * Tells the factory that the phase alphabet of the current phase is complete and the next phase starts.
     */
    void nextPhase();

    S createTerminal(final int phase, final long length, final long weight, final Pair<S, S> pair);
    S createTerminal(final int phase, final long length, final long weight, final Block<S> block);
    S createTerminal(final int phase, final long length, final long weight, final S symbol);

    S createTerminal(final int phase, final long length, final long weight);
    S createTerminal(S symbol, final long length);
    S createTerminal(N name, int phase, long length, long weight);

    /**
     * Creates and returns a fresh non-terminal, this should be used only by CFGCreators.
     *
     * @return a fresh non-terminal
     */
    S createFreshNonTerminal();

    /**
     * Creates a new terminal or non-terminal symbol of the base alphabet.
     *
     * @param name      the identifier of the symbol
     * @param terminal  indicates if the symbol is a terminal or a non-terminal
     * @return a new terminal or non-terminal symbol of the base alphabet
     */
    S makeSymbol(N name, boolean terminal);

    /**
     * Creates a fresh non-terminal.
     *
     * @return a fresh non-terminal
     */
    S makeSymbol();

    /**
     * Returns the current largest id of a symbol of a phase of the recompression algorithm.
     *
     * @param phase the phase of the recompression algorithm
     * @return largest id of a symbol of a phase of the recompression algorithm
     */
    int getMax(final int phase);

    /**
     * Returns the largest id of a non-terminal symbol.
     *
     * @return
     */
    int getMaxNonterminal();

    /**
     * Returns the phase alphabet of a phase.
     *
     * @param phase the phase of the recompression algorithm
     * @return the phase alphabet of a phase
     */
    List<S> getAlphabet(final int phase);

    /**
     * Returns the all non-terminals contained in the factory.
     *
     * @return the all non-terminals contained in the factory
     */
    List<S> getNonTerminals();
}
