package grammar.impl;

import grammar.gen.GenProduction;
import symbol.IJezSymbol;

/**
 * The standard implementation of GenProduction.
 *
 * @author Benedikt Zoennchen
 *
 * @param <N> the type of terminal and non-terminal symbols.
 */
public class Production<N> extends GenProduction<N, IJezSymbol<N>, JezWord<N>>{
    public Production(final IJezSymbol<N> left, final JezWord<N> right) {
        super(left, right);
    }

    @Override
    public Production<N> clone() {
        GenProduction<N, IJezSymbol<N>, JezWord<N>> clone = super.clone();
        return new Production<>(clone.getLeft(), clone.getRight());
    }
}
