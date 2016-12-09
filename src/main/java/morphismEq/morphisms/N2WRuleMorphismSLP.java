package morphismEq.morphisms;

import grammar.impl.SLP;

/**
 * A GenN2WRuleMorphism where T is in specified. T is equal to SLP<N>,
 * so a output word of a N2W rule is a SLP of type N.
 *
 * @author Benedikt Zoennchen
 */
public class N2WRuleMorphismSLP<N> extends GenN2WRuleMorphism<N, SLP<N>> {

    public N2WRuleMorphismSLP(final boolean firstRule) {
        super(firstRule);
    }

    @Override
    protected SLP<N> outputToSLP(final SLP<N> output) {
        return output ;
    }
}
