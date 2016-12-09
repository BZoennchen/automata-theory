package LTW.impl;

import LTW.gen.GenLTWTransformation;
import N2W.impl.reduction.*;
import grammar.impl.*;
import morphismEq.morphisms.N2WRuleMorphismSLP;
import symbol.IJezSymbol;
import symbol.StdState;

/**
 * @author Benedikt Zoennchen
 */
public class LTWTransformation extends GenLTWTransformation<
        Character,
        Character,
        Character,
        Integer,

        RankedSymbol,
        StdState,
        LTWRule,
        LTW,

        N2WState,
        NestedLetter,
        StackSymbol,
        N2WRule,
        N2W,

        IJezSymbol<Object>,
        JezWord<Object>,
        Production<Object>,
        CFG<Object>,
        SLP<Object>,

        IJezSymbol<Character>,
        JezWord<Character>,
        Production<Character>,
        CFG<Character>,
        SLP<Character>
        > {

    /**
     *
     * @param ltwCreator    a linear tree to word transducer creator for introducing new rules and states, this has to be the creator that creates the ltw
     * @param factory
     * @param basefactory
     */
    public LTWTransformation(final LTWCreator ltwCreator,
                             final CFGCreatorFactory<Character> factory,
                             final CFGCreatorFactory<Object> basefactory) {
        super(new N2WRuleMorphismSLP(true), new N2WRuleMorphismSLP(false), ltwCreator, new STWtoN2WReduction(), new N2WCreator(), new N2WtoCFGReduction(basefactory), factory, basefactory);
    }
}