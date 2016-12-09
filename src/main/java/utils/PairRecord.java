package utils;

import data.Node;

/**
 * A pair record combines a pair with a pointer, pointing to the position of the pair occurrence.
 *
 * @author Benedikt Zoennchen
 *
 * @param <S> type of the left pair entry
 * @param <T> type of the right pair entry
 */
public class PairRecord<S, T> {
    public final Pair<S, T> pair;
    public final Node<S> node;

    public PairRecord(final Pair<S, T> pair, final Node<S> node) {
        this.pair = pair;
        this.node = node;
    }

    public boolean equals(final PairRecord record) {
        if(record == null) {
            return false;
        }
        return pair.equals(record.pair);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof PairRecord) {
            return pair.equals(((PairRecord<S, T>)obj).pair);
        }
        return false;
    }

    @Override
    public String toString() {
        return pair + "->" + node;
    }
}
