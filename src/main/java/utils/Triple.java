package utils;

/**
 * A simple triple.
 *
 * @author Benedikt Zoennchen
 *
 * @param <A> type of the first component
 * @param <B> type of the second component
 * @param <C> type of the third component
 */
public class Triple<A, B, C> {
    public final A a;
    public final B b;
    public final C c;

    public Triple(final A a, final B b, final C c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Triple) {
            Triple other = (Triple<A, B, C>)obj;
            return a.equals(other.a) && b.equals(other.b) && c.equals(other.c);
        }
        return false;
    }

    @Override
    public String toString() {
        return "("+a+","+b+","+c+" )";
    }

}