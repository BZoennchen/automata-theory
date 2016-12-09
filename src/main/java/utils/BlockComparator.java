package utils;

import symbol.IJezSymbol;


import java.util.Comparator;

/**
 * Comparator for comparing blocks i.e. first compare the symbol id, secondly compare the lengths.
 *
 * @author Benedikt Zoennchen
 *
 * @param <N> the type of the symbol identifier
 * @param <S> the type of the symbol
 */
public class BlockComparator<N, S extends IJezSymbol<N>> implements Comparator<Block<S>> {

    @Override
    public int compare(final Block<S> o1, final Block<S> o2) {
        if(o1.letter.getId() < o2.letter.getId()) {
            return -1;
        }
        else if(o1.letter.getId() > o2.letter.getId()) {
            return 1;
        }
        else {
            if(o1.length > o2.length) {
                return -1;
            }
            else if(o1.length < o2.length) {
                return 1;
            }
            else {
                return 0;
            }
        }
    }
}
