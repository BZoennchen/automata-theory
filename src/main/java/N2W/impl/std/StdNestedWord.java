package N2W.impl.std;

import N2W.gen.GenNestedLetter;

/**
 * Standard implementation of a GenNestedLetter, its identifier is an Character.
 *
 * @author Benedikt Zoennchen
 */
public class StdNestedWord extends GenNestedLetter<Character> {
    public StdNestedWord(Character element, boolean opening) {
        super(element, opening);
    }
}
