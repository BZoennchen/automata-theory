package LTW.inter;

import grammar.impl.SLP;
import symbol.INamedSymbol;

import java.util.List;
import java.util.function.Function;

/**
 * A rule of a LTW i.e. q(f) -> u_0 q_1(x_?) u_1 q_2 (x_?) ... u_n
 *
 * @author Benedikt Zoennchen
 *
 * @param <N> the type of the output alphabet
 * @param <A> the type of the identifier of the ranked symbol
 * @param <B> the type of the identifier of the state of the LTW
 * @param <F> the type of the ranked symbol
 * @param <Q> the type of the states of the LTW
 */
public interface ILTWRule<N, A, B, F extends IRankedSymbol<A>, Q extends INamedSymbol<B>> {

    /**
     * Returns the source state of the rule.
     *
     * @return the source state of the rule
     */
    Q getSrcState();

    /**
     * Returns the destination states of the rule (in order).
     *
     * @return the destination states of the rule
     */
    List<Q> getDestStates();

    /**
     * Returns the output words of the rule (in order). We use SLP-compressed output words.
     *
     * @return the output words of the rule
     */
    List<SLP<N>> getOutputWords();

    /**
     * Returns the ranked symbol of the rule (i.e. the input symbol).
     *
     * @return the ranked symbol of the rule
     */
    F getSymbol();

    /**
     * The permutation of the rule which defines the re-ordering of the output.
     *
     * @return permutation of the rule
     */
    Function<Integer, Integer> getInputPermutation();
}
