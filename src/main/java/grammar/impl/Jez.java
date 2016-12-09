package grammar.impl;

import grammar.gen.GenJez;
import grammar.inter.ICFG;
import grammar.inter.ICFGCreatorFactory;
import grammar.inter.ISLP;
import symbol.IJezSymbol;

/**
 * The standard implementation of GenJez.
 *
 * @author Benedikt Zoennchen
 *
 * @param <N> the type of terminal and non-terminal symbols.
 */
public class Jez<N> extends GenJez<N, IJezSymbol<N>, JezWord<N>, Production<N>> {

    public Jez(ISLP<N, IJezSymbol<N>, JezWord<N>, Production<N>> slp,
               ICFGCreatorFactory<N, IJezSymbol<N>, JezWord<N>, Production<N>, ICFG<N, IJezSymbol<N>, JezWord<N>, Production<N>>, ISLP<N, IJezSymbol<N>, JezWord<N>, Production<N>>> factory) {
        super(slp, factory);
    }
}
