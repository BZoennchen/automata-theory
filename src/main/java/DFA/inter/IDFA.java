package DFA.inter;

import symbol.INamedSymbol;

import java.util.Set;

/**
 * A deterministic finite word automaton.
 *
 * @author Benedikt Zoennchen
 *
 * @param <N>   the type of the identifier of the letters of the input alphabet of the DFA.
 * @param <B>   the type of the identifier of the states of the DFA.
 * @param <F>   the type of the letter of the input alphabet of the DFA.
 * @param <Q>   the type of the states of the DFA.
 * @param <R>   the type of the transition rules of the DFA.
 *
 */
public interface IDFA<N, B, F extends INamedSymbol<N>, Q extends INamedSymbol<B>, R extends IDFARule<N, B, F, Q>> {

    /**
     * Returns the set of transition rules (q(s) -> p) of the DFA.
     *
     * @return the set of transition rules of the DFA
     */
    Set<R> getRules();

    /**
     * Returns the initial state of the DFA.
     *
     * @return the set of initial states
     */
    Q getInitialState();

    /**
     * Returns the final states of the DFA.
     *
     * @return the final states of the DFA
     */
    Set<Q> getFinalStates();

    /**
     * Returns the set of states of the DFA.
     *
     * @return the set of states of the DFA
     */
    Set<Q> getStates();

    /**
     * Returns the set of symbols of the DFA, i.e. the input alphabet of the DFA.
     *
     * @return the set of symbols of the DFA
     */
    Set<F> getSymbols();
}
