package grammar.gen;

import grammar.inter.IReferencedWord;
import grammar.inter.IProduction;
import symbol.IJezSymbol;

/**
 * The generic implementation of a grammar production.
 *
 * @author Benedikt Zoennchen
 *
 * @param <N>   the type of the identifier of terminal and non-terminal symbols of the grammar
 * @param <S>   the type of the terminal and non-terminal symbols of the grammar
 * @param <W>   the type of the right-hand side of the grammar
 */
public class GenProduction<N, S extends IJezSymbol<N>, W extends IReferencedWord<N, S>> implements IProduction<N, S, W> {

    /**
     * The left-hand side of the production.
     */
    private S left;

    /**
     * The right-hand side of the production.
     */
    private W right;

    /**
     * The default constructor of a generic grammar production.
     *
     * @param left  the left-hand side of the production
     * @param right the right-hand side of the production
     */
    public GenProduction(final S left, final W right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public S getLeft() {
        return left;
    }

    @Override
    public W getRight() {
        return right;
    }

    @Override
    public GenProduction<N, S, W> clone() {
        return new GenProduction<>((S)getLeft().clone(), (W)getRight().clone());
    }

    @Override
    public int getNumberOfNonTerminals() {
        return right.findAll(s -> !s.isTerminal()).size();
    }

    @Override
    public int getNumberOfTerminals() {
        return right.findAll(s -> s.isTerminal()).size();
    }

    @Override
    public String toString() {
        return left + "->" + right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GenProduction<?, ?, ?> that = (GenProduction<?, ?, ?>) o;

        if (!left.equals(that.left)) return false;
        return right.equals(that.right);

    }

    @Override
    public int hashCode() {
        return left.hashCode();
    }
}
