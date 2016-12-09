package N2W.impl.std;

import N2W.gen.GenStackSymbol;

/**
 * Standard implementation of a GenStackSymbol, its identifier is an Integer.
 *
 * @author Benedikt Zoennchen
 */
public class StdStackSymbol extends GenStackSymbol<Integer> {
    public StdStackSymbol(Integer element) {
        super(element);
    }
}
