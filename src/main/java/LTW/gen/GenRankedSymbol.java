package LTW.gen;

import LTW.inter.IRankedSymbol;

/**
 * A input symbol of a LTW.
 *
 * @author Benedikt Zoennchen
 *
 * @param <A> the type of the identifier of the ranked symbol
 */
public class GenRankedSymbol<A> implements IRankedSymbol<A> {

    /**
     * The identifier of the ranked input symbol.
     */
    private A element;

    /**
     * The rank of the input symbol.
     */
    private int arity;

    public GenRankedSymbol(final A element, final int arity) {
        this.element = element;
        this.arity = arity;
    }

    @Override
    public int getArity() {
        return arity;
    }

    @Override
    public A getElement() {
        return element;
    }

    @Override
    public String toString() {
        return element.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GenRankedSymbol<?> that = (GenRankedSymbol<?>) o;

        if (arity != that.arity) return false;
        return !(element != null ? !element.equals(that.element) : that.element != null);
    }

    @Override
    public int hashCode() {
        int result = element != null ? element.hashCode() : 0;
        result = 31 * result + arity;
        return result;
    }
}
