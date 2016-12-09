package grammar.impl;

import grammar.inter.ICFG;
import grammar.inter.ICFGCreatorFactory;
import symbol.IJezSymbol;

import java.util.Collection;
import java.util.LinkedList;

/**
 * The standard definition of a factory creator.
 *
 * @author Benedikt Zoennchen
 *
 * @param <N> the type of terminal and non-terminal symbols.
 */
public class CFGCreatorFactory<N> implements ICFGCreatorFactory<N, IJezSymbol<N>, JezWord<N>, Production<N>, CFG<N>, SLP<N>> {

    @Override
    public CFGCreator<N> create() {
        return new CFGCreator<>();
    }

    @Override
    public CFGCreator<N> create(final Collection<? extends ICFG<N, ?, ?, ?>> cfgs) {
        return new CFGCreator<>(cfgs);
    }

    @Override
    public CFGCreator<N> create(final ICFG<N, ?, ?, ?>... cfg) {
        LinkedList<ICFG<N, ?, ?, ?>> cfgs = new LinkedList<>();
        for(ICFG<N, ?, ?, ?> s : cfg) {
            cfgs.add(s);
        }
        return create(cfgs);
    }
}
