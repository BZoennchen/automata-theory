package N2W.impl.reduction;

import LTW.impl.RankedSymbol;
import N2W.gen.GenNestedLetter;

/**
 * Reduction implementation of a GenNestedLetter, its identifier a ranked symbol of the LTW.
 *
 * @author Benedikt Zoennchen
 */
public class NestedLetter extends GenNestedLetter<RankedSymbol> {
    public NestedLetter(RankedSymbol element, boolean opening) {
        super(element, opening);
    }
}