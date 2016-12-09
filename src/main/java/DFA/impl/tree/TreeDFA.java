package DFA.impl.tree;

import DFA.gen.GenDFA;
import symbol.INamedSymbol;
import utils.Pair;

import java.util.Set;

/**
 * A standard implementation of the GenDFA which we use for the reduction
 * of the equivalence problem of deterministic top-down tree automaton.
 * We convert the deterministic top-down tree automaton into a DFA.
 *
 * Input-type: (tree symbol of identifier type B, Integer),
 * State-type: <B>
 *
 * @param <F>   the type of the tree symbol of a tree automaton
 * @param <B>   the type of the identifier of the state of the tree automaton.
 * @param <Q>   the type of the state of a tree automaton.
 *
 * @author Benedikt Zoennchen
 */
public class TreeDFA<F, B, Q extends INamedSymbol<B>> extends GenDFA<Pair<F, Integer>, B, TreeDFASymbol<F>, Q, TreeDFARule<F, B, Q>> {

    public TreeDFA(Set<TreeDFARule<F, B, Q>> rules, Q initialStates, Set<Q> finalStates) {
        super(rules, initialStates, finalStates);
    }
}
