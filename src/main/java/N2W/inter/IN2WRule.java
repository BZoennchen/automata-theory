package N2W.inter;

import symbol.INamedSymbol;

/**
 * A rule of a N2W i.e. q --(f:?/u:j)--> p, where f is a symbol ? is closing or opening, u is the output word
 * and j is a stack symbol.
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
 */
public interface IN2WRule<N, A, B, C, Q extends INamedSymbol<A>, W extends INestedLetter<B>, G extends INamedSymbol<C>> {

    /**
     * Returns the start state of the rule, i.e. q.
     *
     * @return the start state of the rule
     */
    Q getStartState();

    /**
     * Returns the end state of the rule, i.e. p.
     *
     * @return the end state of the rule
     */
    Q getEndState();

    /**
     * Returns the nested word of the rule.
     *
     * @return the nested word of the rule
     */
    W getNestedWord();

    /**
     * Returns the stack symbol of the rule.
     *
     * @return the stack symbol of the rule
     */
    G getStackSymbol();

    /**
     * Returns the output word of the of the rule.
     *
     * @return the output word of the of the rule
     */
    N getOutputWord();
}
