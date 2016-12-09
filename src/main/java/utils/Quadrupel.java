package utils;

/**
 * A simple quadruple.
 *
 * @author Benedikt Zoennchen
 *
 * @param <A> type of the first component
 * @param <B> type of the second component
 * @param <C> type of the third component
 * @param <D> type of the fourth component
 */
public class Quadrupel<A, B, C, D> {
    public final A a;
    public final B b;
    public final C c;
    public final D d;

    public Quadrupel(final A a, final B b, final C c, final D d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Quadrupel<?, ?, ?, ?> quadrupel = (Quadrupel<?, ?, ?, ?>) o;

        if (a != null ? !a.equals(quadrupel.a) : quadrupel.a != null) return false;
        if (b != null ? !b.equals(quadrupel.b) : quadrupel.b != null) return false;
        if (c != null ? !c.equals(quadrupel.c) : quadrupel.c != null) return false;
        return !(d != null ? !d.equals(quadrupel.d) : quadrupel.d != null);

    }

    @Override
    public int hashCode() {
        int result = a != null ? a.hashCode() : 0;
        result = 31 * result + (b != null ? b.hashCode() : 0);
        result = 31 * result + (c != null ? c.hashCode() : 0);
        result = 31 * result + (d != null ? d.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "("+a+","+b+","+c+","+d+" )";
    }
}
