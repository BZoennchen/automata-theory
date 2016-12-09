package morphismEq.impl;

import grammar.impl.Production;
import grammar.impl.CFG;
import grammar.impl.JezWord;
import grammar.impl.SLP;
import grammar.inter.ICFGCreator;
import grammar.inter.ICFGCreatorFactory;
import morphismEq.gen.GenMorphismEQSolver;
import symbol.IJezSymbol;

/**
 * Default implementation of the morphism equivalence test on CFGs.
 *
 * @author Benedikt Zoennchen
 */
public class MorphismEQSolver<N1, N2> extends GenMorphismEQSolver<
        N1, IJezSymbol<N1>, JezWord<N1>, Production<N1>, CFG<N1>, SLP<N1>,
        N2, IJezSymbol<N2>, JezWord<N2>, Production<N2>,   CFG<N2>, SLP<N2>>
{
    /**
     * @param cfg        the base cfg G
     * @param cfgCreator s
     * @param slpCreator
     */
    public MorphismEQSolver(
            final CFG<N1> cfg,
            final ICFGCreator<N1, IJezSymbol<N1>, JezWord<N1>, Production<N1>, CFG<N1>, SLP<N1>> cfgCreator,
            final ICFGCreatorFactory<N2, IJezSymbol<N2>, JezWord<N2>, Production<N2>, CFG<N2>, SLP<N2>> slpCreator,
            final ICFGCreatorFactory<N1, IJezSymbol<N1>, JezWord<N1>, Production<N1>, CFG<N1>, SLP<N1>> baseSlpCreator) {
        super(cfg, cfgCreator, slpCreator, baseSlpCreator);
    }
}

