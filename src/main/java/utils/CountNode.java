package utils;

import data.Node;
import symbol.Symbol;

/**
 * A helper class for combining a non-terminal (letter) of a rhs,
 * a counter of the unmarked non-terminals in the rhs
 * and the left non-terminal (ruleLetter) of the rule, so the lhs.
 * This is also for the data structure introduced by Hopcroft.
 *
 * @author Benedikt Zoennchen
 *
 * @param <S> the type of the symbol
 */
public class CountNode<S extends Symbol> {

    /**
     * A non-terminal.
     */
    public S ruleLetter;

    /**
     * Identifies a rule.
     */
    public int ruleId;

    /**
     * The counter of the node.
     */
    public Counter count;

    /**
     * The pointer to the symbol of the node.
     */
    public Node<S> letter;

}