package utils;

/**
 * A pair couple.
 *
 * @author Benedikt Zoennchen
 *
 * @param <T> type of the left pair entry
 * @param <E> type of the right pair entry
 */
public class Pair<T , E> {
    public final T a;
    public final E b;

    public Pair(final T a, final E b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pair<?, ?> pair = (Pair<?, ?>) o;

        if (a != null ? !a.equals(pair.a) : pair.a != null) return false;
        return !(b != null ? !b.equals(pair.b) : pair.b != null);

    }

    @Override
    public int hashCode() {
        int result = a != null ? a.hashCode() : 0;
        result = 31 * result + (b != null ? b.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "("+a+","+b+")";
    }
}
