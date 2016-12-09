package LTW.inter;

import grammar.impl.SLP;
import symbol.INamedSymbol;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * The ILTWCreator is a factory for the creation of LTWs. Some operations require to introduce fresh states.
 * Such states has to be distinct from all states used ba a grammar. Therefore operations require often to
 * use the creator that creates a certain LTW! Otherwise we would have to initialize a new creator and add all
 * states of the LTWs beforehand which costs is running time which we want to avoid.
 *
 * @author Benedikt Zoennchen
 *
 * @param <N> the type of the output alphabet
 * @param <A> the type of the identifier of the ranked symbol
 * @param <B> the type of the identifier of the state of the LTW
 * @param <F> the type of the ranked symbol
 * @param <Q> the type of the states of the LTW
 * @param <R> the type of the rules of the LTW
 * @param <L> the type of the LTW
 */
public interface ILTWCreator<N, A, B, F extends IRankedSymbol<A>, Q extends INamedSymbol<B>, R extends ILTWRule<N, A, B, F, Q>, L extends ILTW<N, A, B, F, Q, R>> {

    /**
     * Creates a new state, if there is no state with the name in use or the state identified by the name.
     *
     * @param name  the identifier of the state
     * @return a state identified by the name
     */
    Q createState(B name);

    /**
     * Creates a fresh unused state.
     *
     * @return a fresh unused state
     */
    Q createFreshState();

    /**
     * Creates a new LTW rule. The size list of output words has
     * to equal to the size of the destination states + 1.
     *
     * @param srcState      the source state of the rule
     * @param symbol        the input symbol of the rule
     * @param destState     the destination states of the rule
     * @param outputWords   the output words of the rule
     * @param permutation   the permutation of the rule
     * @return
     */
    R createRule(Q srcState, F symbol, List<Q> destState, List<SLP<N>> outputWords, Function<Integer, Integer> permutation);

    /**
     * Creates a new LTW defined by the set of rules and the set of initial states.
     *
     * @param rules         the set of rules
     * @param initialStates the set of initial states
     * @return a new LTW
     */
    L createLTW(final Set<R> rules, final Set<Q> initialStates);

    /**
     * Creates a new LTW defined by the set of rules and the initial state.
     *
     * @param rules         the set of rules
     * @param initialState  a single initial state
     * @return a new LTW
     */
    L createLTW(final Set<R> rules, final Q initialState);
}
