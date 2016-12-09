package utils;


import data.Node;

/**
 * A pair record combines a pair with a pointer, pointing to the position of the pair occurrence.
 * Furthermore it contains a flag indicating that the occurrence of the pair is crossing or not
 * and a variable to store the numbers of occurrences of this pair in val(S). The last property is
 * for the greedy pair compression.
 *
 * @author Benedikt Zoennchen
 *
 * @param <S> type of the left pair entry
 * @param <T> type of the right pair entry
 */
public class GPairRecord<S, T> extends PairRecord<S, T> {
    public final boolean crossing;
    public final long appearences;

    public GPairRecord(final Pair<S, T> pair, final Node<S> node, final boolean crossing, final long appearences) {
        super(pair, node);
        this.crossing = crossing;
        this.appearences = appearences;
    }

    public boolean isCrossingPair() {
        return crossing;
    }

    public long getAppearences() {
        return appearences;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        GPairRecord<?, ?> that = (GPairRecord<?, ?>) o;

        return crossing == that.crossing;

    }

    @Override
    public int hashCode() {
        return (crossing ? 1 : 0);
    }

    @Override
    public String toString() {
        return "|"+isCrossingPair() + super.toString()+"|";
    }
}
