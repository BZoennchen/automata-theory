package morphismEq.morphisms;

import grammar.impl.SLP;

import java.util.List;

/**
 * Morphism that replaces terminals of type (r1, r2) (pair of rules of N2Ws) with output words of type N
 * by a list of terminals of type N represented by an SLP of type N.
 *
 * @author Benedikt Zoennchen
 */
public class N2WRuleMorphismList<N> extends GenN2WRuleMorphism<N, List<N>> {

    public N2WRuleMorphismList(boolean firstRule) {
        super(firstRule);
    }

    @Override
    protected SLP<N> outputToSLP(final List<N> output) {
        return slpCreator.oneProduction(output);
    }
}
