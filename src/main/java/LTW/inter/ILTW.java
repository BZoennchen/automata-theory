package LTW.inter;

import symbol.INamedSymbol;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A linear tree-to-word transducer. We use the definition with initial states instead of an axiom.
 * A LTW has to be deterministic!
 *
 * @author Benedikt Zoennchen
 *
 * @param <N> the type of the output alphabet
 * @param <A> the type of the identifier of the ranked symbol
 * @param <B> the type of the identifier of the state of the LTW
 * @param <F> the type of the ranked symbol
 * @param <Q> the type of the states of the LTW
 * @param <R> the type of the rules of the LTW
 */
public interface ILTW<N, A, B, F extends IRankedSymbol<A>, Q extends INamedSymbol<B>, R extends ILTWRule<N, A, B, F, Q>> {

    /**
     * Returns the set of rules of the LTW.
     *
     * @return the set of rules of the LTW
     */
    Set<R> getRules();

    /**
     * Returns a mapping : Q -> set of rules such that q |-> {r in R | lhs(r) = q}.
     *
     * @return mapping : Q -> set of rules such that q |-> {r in R | lhs(r) = q}.
     */
    Map<Q, List<R>>  getRulesBySrcState();

    /**
     * Returns a set of rules R with R = {r in R | lhs(r) = srcState}.
     *
     * @param srcState the source state of the rules
     * @return a set of rules R with R = {r in R | lhs(r) = srcState}.
     */
    Set<R> getRules(final Q srcState);

    /**
     * Returns the set of states of the LTW.
     *
     * @return the set of states of the LTW
     */
    Set<Q> getStates();

    /**
     * Returns the set of initial states of the LTW.
     *
     * @return the set of initial states of the LTW
     */
    Set<Q> getInitialStates();

    /**
     * Returns the input alphabet of the LTW.
     *
     * @return the input alphabet
     */
    Set<F> getInputAlphabet();

    /**
     * Returns the output alphabet of the LTW.
     *
     * @return the output alphabet of the LTW
     */
    Set<N> getOutputAlphabet();
}
