package DFA.impl.tree;

import DFA.gen.GenDFARule;
import symbol.INamedSymbol;
import utils.Pair;

/**
 * A standard implementation of the GenDFARule which we use for the reduction
 * of the equivalence problem of deterministic top-down tree automaton.
 *
 * Input-type: (tree symbol of identifier type B, Integer),
 * State-type: <B>
 *
 * @author Benedikt Zoennchen
 *
 * @param <F>   the type of the tree symbol of a tree automaton
 * @param <B>   the type of the identifier of the state of the tree automaton.
 * @param <Q>   the type of the state of a tree automaton.
 */
public class TreeDFARule<F, B, Q extends INamedSymbol<B>> extends GenDFARule<Pair<F, Integer>, B, TreeDFASymbol<F>, Q> {

    public TreeDFARule(Q srcState, Q destState, TreeDFASymbol symbol) {
        super(srcState, destState, symbol);
    }

}
