package DFA.inter;

import symbol.INamedSymbol;

import java.util.Set;

/**
 * A factory for creating a DFA.
 *
 * @author Benedikt Zoennchen
 *
 * @param <N>   the type of the identifier of the letters of the input alphabet of the DFA
 * @param <B>   the type of the identifier of the states of the DFA
 * @param <S>   the type of the letter of the input alphabet of the DFA
 * @param <Q>   the type of the states of the DFA
 * @param <R>   the type of the transition rules of the DFA
 * @param <D>   the type of the DFA
 */
public interface IDFACreator<N, B, S extends INamedSymbol<N>, Q extends INamedSymbol<B>, R extends  IDFARule<N, B, S, Q>, D extends IDFA<N, B, S, Q, R>> {

    /**
     * Creates a new fresh state.
     *
     * @return a new fresh state
     */
    Q createState();

    /**
     * Creates a state identified by its name. This may be a new state or a state already in use.
     * @param name the identifier of the state
     *
     * @return a state identified by its name
     */
    Q createState(B name);

    /**
     * Creates a letter of the input alphabet identified by its name. This may be a new letter or a letter already in use.
     * @param name the identifier of the letter
     *
     * @return a letter identified by its name
     */
    S createSymbol(N name);

    /**
     * Creates a new transition rule defined by the source, destination state and a input letter i.e. q(a) -> p.
     *
     * @param srcState  the source state q
     * @param destState the destination state p
     * @param symbol    the input letter a
     * @return a new transition rule
     */
    R createRule(Q srcState, Q destState, S symbol);

    /**
     * Creates a new DFA defined by the transition rules, the initial state and the set of final states.
     *
     * @param rules         the transition rules of the DFA
     * @param initialState  the initial state of the DFA
     * @param finalStates   the set of final states of the DFA
     * @return a new DFA
     */
    D create(Set<R> rules, Q initialState, Set<Q> finalStates);
}