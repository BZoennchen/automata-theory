package N2W.impl.reduction;

import LTW.impl.LTWRule;
import N2W.gen.GenN2WState;
import utils.Pair;

/**
 * Reduction implementation of a GenN2WState, its identifier is a pair (r, i), where r is
 * a LTWRule and i is a Integer.
 *
 * @author Benedikt Zoennchen
 */
public class N2WState extends GenN2WState<Pair<LTWRule, Integer>> {
    public N2WState(Pair<LTWRule, Integer> element) {
        super(element);
    }
}
