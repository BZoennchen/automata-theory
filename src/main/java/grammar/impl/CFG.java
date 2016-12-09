package grammar.impl;

import grammar.gen.GenCFG;
import symbol.IJezSymbol;

import java.util.Set;

/**
 * The standard implementation of a CFG.
 *
 * @author Benedikt Zoennchen
 *
 * @param <N> the type of terminal and non-terminal symbols.
 */
public class CFG<N> extends GenCFG<N, IJezSymbol<N>, JezWord<N>, Production<N>> {
    public CFG(final Set<Production<N>> productions, final Set<IJezSymbol<N>> axiom) {
        super(productions, axiom);
    }
}
