package N2W.inter;

import symbol.INamedSymbol;

import java.util.Set;

/**
 * The IN2WCreator is a factory for the creation of N2Ws. Some operations require to introduce fresh states.
 * Such states has to be distinct from all states used by any N2W. Therefore operations require often to
 * use the creator that creates a certain N2W! Otherwise we would have to initialize a new creator and add all
 * states of the N2W beforehand which costs is running time which we want to avoid.
 *
 * @author Benedikt Zoennchen
 *
 * @param <N> the type of the output !word! of the N2W (not the output). The output word might be a SLP of another type.
 * @param <A> the type of the identifier of the state of the N2W
 * @param <B> the type of the identifier of the symbol of the N2W
 * @param <C> the type of the identifier of the stack symbol
 * @param <Q> the type of the state of the N2W
 * @param <W> the type of the nested word of the N2W
 * @param <G> the type of the stack symbol of the N2W
 * @param <R> the type of the rule of the N2W
 * @param <M> the type of the N2W
 */
public interface IN2WCreator<N, A, B, C,
        Q extends INamedSymbol<A>,
        W extends INestedLetter<B>,
        G extends INamedSymbol<C>,
        R extends IN2WRule<N, A, B, C, Q, W, G>,
        M extends IN2WTransducer<N, A, B, C, Q, W, G, R>
        > {

    /**
     * Creates and returns a fresh state.
     *
     * @return a fresh state
     */
    Q createFreshState();

    /**
     * Creates a new state, if there is no state with the name in use or the state identified by the name.
     *
     * @param name  the identifier of the state
     * @return a state identified by the name
     */
    Q createState(final A name);

    /**
     * Creates a new opening or closing nested letter identified by the element.
     *
     * @param element the identifier of the nested letter
     * @param opening true => the letter will be opening, otherwise the letter will be closing
     * @return a new opening or closing nested letter
     */
    W createNestedLetter(final B element, final boolean opening);

    /**
     * Creates a new stack symbol, if there is no stack symbol with the name in use or the stack symbol identified by the name.
     *
     * @param element  the identifier of the state
     * @return a stack symbol identified by the element
     */
    G createStackSymbol(final C element);
    R createRule(final Q srcState, final Q destState, final W nestedLetter, final G stackSymbol, final N outputWord);
    M createTransducer(final Set<R> rules, final Set<Q> initialStates, final Set<Q> finalStates);
}
