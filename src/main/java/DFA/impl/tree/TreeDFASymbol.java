package DFA.impl.tree;

import symbol.GenNamedSymbol;
import utils.Pair;

/**
 * A input letter of the DFA. The letter is a pair (F, Integer).
 *
 * @param <F>   the type of the tree symbol of a tree automaton
 */
public class TreeDFASymbol<F> extends GenNamedSymbol<Pair<F,Integer>> {

    public TreeDFASymbol(final Pair<F, Integer> element) {
        super(element);
    }

}
