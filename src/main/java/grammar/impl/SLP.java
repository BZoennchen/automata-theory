package grammar.impl;

import grammar.gen.GenSLP;
import symbol.IJezSymbol;

import java.util.Map;
import java.util.Set;

/**
 * The standard implementation of GenSLP.
 *
 * @author Benedikt Zoennchen
 *
 * @param <N> the type of terminal and non-terminal symbols.
 */
public class SLP<N> extends GenSLP<N, IJezSymbol<N>, JezWord<N>, Production<N>> {
    public SLP(final Map<IJezSymbol<N>, Production<N>> produtions, final Set<IJezSymbol<N>> axioms) {
        super(produtions, axioms);
    }

    public SLP(final Set<Production<N>> produtions, final Set<IJezSymbol<N>> axioms) {
        super(produtions, axioms);
    }

    @Override
    public SLP<N> clone() {
        GenSLP<N, IJezSymbol<N>, JezWord<N>, Production<N>> genSLP = super.clone();
        return new SLP<>(genSLP.getProductions(), genSLP.getAxioms());
    }
}
