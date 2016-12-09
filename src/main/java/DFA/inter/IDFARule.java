package DFA.inter;

import symbol.INamedSymbol;

/**
 * A transition rule (q(s) -> p) of a DFA where q is the source state, s is the input letter and p
 * is the destination state.
 *
 * @author Benedikt Zoennchen
 *
 * @param <N> the type of the identifier of the letter of the input alphabet (e.g. char).
 * @param <B> the type of the identifier of the states (e.g. a String, char, Integer).
 * @param <S> the type of the input symbol.
 * @param <Q> type of the states, they are at least of the type INamedSymbol.
 */
public interface IDFARule<N, B, S extends INamedSymbol<N>, Q extends INamedSymbol<B>> {

    /**
     * Returns the input letter <b>s</b> of the rule.
     *
     * @return the input letter of the rule
     */
    S getSymbol();

	/**
	 * Returns the destination state <b>p</b> of the rule.
	 *
	 * @return the destination state of the rule
	 */
	Q getDestState();

	/**
	 * Returns the source state <b>q</b> of the rule.
	 *
	 * @return the source state of the rule
	 */
    Q getSrcState();
}
