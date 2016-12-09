package N2W.impl.reduction;

import LTW.impl.LTWRule;
import N2W.gen.GenStackSymbol;
import utils.Pair;

/**
 * Reduction implementation of a GenStackSymbol, its identifier is a pair (r, i), where r is
 * a LTWRule and i is a Integer.
 *
 * @author Benedikt Zoennchen
 */
public class StackSymbol extends GenStackSymbol<Pair<LTWRule, Integer>> {
    public StackSymbol(final Pair<LTWRule, Integer> element) {
        super(element);
    }
}
