package DFA.impl.tree;

import DFA.gen.GenDFAOp;
import symbol.INamedSymbol;
import utils.Pair;

/**
 * The GenDFAOp implementation to work with DFAs constructed out of deterministic top-down tree automaton.
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
public class TreeDFAOp<F, B, Q extends INamedSymbol<B>> extends GenDFAOp<Pair<F, Integer>, B, TreeDFASymbol<F>, Q, TreeDFARule<F, B, Q>, TreeDFA<F, B, Q>> {}
