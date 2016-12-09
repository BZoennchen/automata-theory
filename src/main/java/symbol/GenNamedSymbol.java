package symbol;

import com.sun.istack.internal.NotNull;

/**
 * @author Benedikt Zoennchen
 */
public class GenNamedSymbol<T> implements INamedSymbol<T> {
    private @NotNull T element;
    private final int hashCode;

    public GenNamedSymbol(final @NotNull T element) {
        this.element = element;
        this.hashCode = calcHash();
    }

    @Override
    public T getName() {
        return element;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof GenNamedSymbol<?>)) return false;

        GenNamedSymbol<?> that = (GenNamedSymbol<?>) o;

        if (hashCode != that.hashCode) return false;
        return !(element != null ? !element.equals(that.element) : that.element != null);

    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    private int calcHash() {
        int result = element != null ? element.hashCode() : 0;
        result = 31 * result + hashCode;
        return result;
    }

    @Override
    public String toString() {
        return element.toString();
    }

}


