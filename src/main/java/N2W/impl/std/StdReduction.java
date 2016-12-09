package N2W.impl.std;

import N2W.gen.GenN2WtoCFGReduction;
import grammar.impl.CFG;
import grammar.impl.JezWord;
import grammar.impl.Production;
import grammar.impl.SLP;
import grammar.inter.ICFGCreatorFactory;
import symbol.IJezSymbol;
import symbol.StdState;

import java.util.*;

/**
 * Standard implementation of a GenN2WtoCFGReduction.
 *
 * @author Benedikt Zoennchen
 */
public class StdReduction extends GenN2WtoCFGReduction<
        StdNestedWord,
        List<StdNestedWord>,
        Integer, Character, Integer, StdState, StdNestedWord, StdStackSymbol, StdN2WRule, StdN2W,

        IJezSymbol<Object>,
        JezWord<Object>,
        Production<Object>,
        CFG<Object>,
        SLP<Object>,

        IJezSymbol<StdNestedWord>,
        JezWord<StdNestedWord>,
        Production<StdNestedWord>,
        CFG<StdNestedWord>,
        SLP<StdNestedWord>> {


    public StdReduction(final ICFGCreatorFactory<Object, IJezSymbol<Object>, JezWord<Object>, Production<Object>, CFG<Object>, SLP<Object>> factory) {
        super(factory);
    }
}
