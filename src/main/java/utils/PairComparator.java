package utils;

import symbol.IJezSymbol;

import java.util.Comparator;

/**
 * A comparator for comparing pairs i.e. we first compare the first components and secondly the second components.
 *
 * @author Benedikt Zoennchen
 *
 * @param <S> type of the left pair entry
 * @param <T> type of the right pair entry
 */
public class PairComparator<S extends IJezSymbol, T extends IJezSymbol> implements Comparator<Pair<S, T>> {
    @Override
    public int compare(final Pair<S, T> o1, final Pair<S, T> o2) {
        if(o1.a.getId() < o2.a.getId() || (o1.a.getId() == o2.a.getId()) && o1.b.getId() < o2.b.getId()) {
            return -1;
        }
        else if(o1.a.getId() > o2.a.getId() || (o1.a.getId() == o2.a.getId()) && o1.b.getId() > o2.b.getId()) {
            return 1;
        }
        else {
            return 0;
        }
    }
}
