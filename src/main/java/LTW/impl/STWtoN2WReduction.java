package LTW.impl;


import N2W.impl.reduction.*;
import LTW.gen.GenSTWtoN2W;
import symbol.StdState;

/**
 * @author Benedikt Zoennchen
 */
public class STWtoN2WReduction extends GenSTWtoN2W<
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
        N2W> {
}
