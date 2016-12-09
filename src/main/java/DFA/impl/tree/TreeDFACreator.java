package DFA.impl.tree;

import DFA.inter.IDFACreator;
import symbol.INamedSymbol;
import utils.Pair;

import java.util.Set;

/**
 * A IDFACreator for creating DFAs for the reduction of deterministic top-down tree automaton to DFA.
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
public class TreeDFACreator<F, B, Q extends INamedSymbol<B>> implements IDFACreator<Pair<F, Integer>, B, TreeDFASymbol<F>, Q, TreeDFARule<F, B, Q>, TreeDFA<F, B, Q>> {

    @Override
    public Q createState() {
        throw new UnsupportedOperationException("not supported.");
    }

    @Override
    public Q createState(B name) {
        throw new UnsupportedOperationException("not supported.");
    }

    @Override
    public TreeDFASymbol<F> createSymbol(final Pair<F, Integer> name) {
        return new TreeDFASymbol<>(name);
    }

    @Override
    public TreeDFARule<F, B, Q> createRule(final Q srcState, final Q destState, final TreeDFASymbol<F> symbol) {
        return new TreeDFARule<>(srcState, destState, symbol);
    }

    @Override
    public TreeDFA<F, B, Q> create(final Set<TreeDFARule<F, B, Q>> rules, final Q initialState, final Set<Q> finalStates) {
        return new TreeDFA<>(rules, initialState, finalStates);
    }
}