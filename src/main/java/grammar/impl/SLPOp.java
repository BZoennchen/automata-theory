package grammar.impl;

import grammar.gen.GenSLPOp;
import symbol.IJezSymbol;

/**
 * The standard implementation of GenSLPOp.
 *
 * @author Benedikt Zoennchen
 *
 * @param <N> the type of terminal and non-terminal symbols.
 */
public class SLPOp<N> extends GenSLPOp<N, IJezSymbol<N>, JezWord<N>, Production<N>, CFG<N>, SLP<N>> {

    /**
     * Tests whether the words of the SLPs are all equal.
     *
     * @param slp           the SLP defining a set of words
     * @param cfgCreator    the creator of the SLPs
     * @return true => all SLPs define the same word, otherwise false
     */
    public boolean equal(final SLP<N> slp, final CFGCreatorFactory<N> cfgCreator) {
        Jez jez = new Jez(slp, cfgCreator);
        return jez.isEquals();
    }

}
