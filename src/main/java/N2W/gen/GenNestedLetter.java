package N2W.gen;

import N2W.inter.INestedLetter;

/**
 * The generic immutable implementation of a nested letter of the N2W.
 *
 * @author Benedikt Zoennchen
 *
 * @param <B> the type of the identifier of the symbol of the N2W
 */
public class GenNestedLetter<B> implements INestedLetter<B> {

    /**
     * The identifier of the nested letter.
     */
    private final B element;

    /**
     * True => the nested letter is a opening nested letter, otherwise the letter is a closing nested letter.
     */
    private final boolean opening;

    /**
     * The hash value of the nested letter.
     */
    private final int hashCode;

    public GenNestedLetter(final B element, final boolean opening) {
        if(element == null) {
            throw new IllegalArgumentException("element of a nested word is null.");
        }

        this.hashCode = calcHash();
        this.element = element;
        this.opening = opening;
    }

    @Override
    public B getElement() {
        return element;
    }

    @Override
    public boolean isOpening() {
        return opening;
    }

    @Override
    public boolean isClosing() {
        return !isOpening();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GenNestedLetter<?> that = (GenNestedLetter<?>) o;

        if (opening != that.opening) return false;
        return !(element != null ? !element.equals(that.element) : that.element != null);

    }

    private int calcHash() {
        int result = element != null ? element.hashCode() : 0;
        result = 31 * result + (opening ? 1 : 0);
        return result;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return isOpening() ? "(" + element.toString() : element.toString() + ")";
    }
}
