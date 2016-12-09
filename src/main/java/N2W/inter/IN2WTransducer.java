package N2W.inter;

import symbol.INamedSymbol;

import java.util.Collection;
import java.util.Set;

/**
 * A N2W (nested word-to-word transducers).
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
 *
 */
public interface IN2WTransducer<N, A, B, C, Q extends INamedSymbol<A>, W extends INestedLetter<B>, G extends INamedSymbol<C>, R extends IN2WRule<N, A, B, C, Q, W, G>>  {

    /**
     * Returns the set of rules of the N2W.
     *
     * @return the set of rules
     */
    Set<R> getRules();

    /**
     * Returns the set of opening rules.
     *
     * @return the set of opening rules
     */
    Set<R> getOpeningRules();

    /**
     * Returns the set of closing rules.
     *
     * @return the set of closing rules
     */
    Set<R> getClosingRules();

    /**
     * Returns the set of initial states.
     *
     * @return the set of initial states
     */
    Set<Q> getInitialStates();

    /**
     * Returns the set of final states.
     *
     * @return the set of final states
     */
    Set<Q> getFinalStates();

    /**
     * Returns the set of states.
     *
     * @return the set of states
     */
    Set<Q> getStates();

    /**
     * Returns the input alphabet.
     *
     * @return the input alphabet
     */
    Set<B> getInputAlphabet();

    /**
     * Returns the collection of output words, the collection may contain duplicates.
     *
     * @return the input alphabet
     */
    Collection<N> getOutputWords();

    /**
     * Returns the stack alphabet.
     *
     * @return the stack alphabet
     */
    Set<G> getStackAlphabet();

}
