package grammar.inter;

import data.Node;
import symbol.IJezSymbol;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A mutable word of the grammar that supports pointers to its symbols e.g. the right-hand side of a grammar production.
 *
 * @author Benedikt Zoennchen
 *
 * @param <N>   the type of the identifier of terminal and non-terminal symbols of the grammar
 * @param <S>   the type of the terminal and non-terminal symbols of the grammar
 */
public interface IReferencedWord<N, S extends IJezSymbol<N>> extends IWord<S> {

    /**
     * A stream of pointers pointing to the symbols of this word.
     *
     * @return a stream of pointers pointing to the symbols of this word
     */
    Stream<Node<S>> nodeStream();

    /**
     * A iterator to iterate over pointers pointing to the symbols of this word
     *
     * @return a iterator to iterate over pointers pointing to the symbols of this word
     */
    Iterator<Node<S>> nodeIterator();

    /**
     * Returns a List of pointers pointing to symbols of this word satisfying the predicate condition.
     *
     * @param pred the predicate condition
     * @return a List of pointers pointing to symbols
     */
    List<Node<S>> findAllPointers(final Predicate<S> pred);

    /**
     * Deletes the a prefix of symbols that all satisfy the predicate condition.
     *
     * @param pred  the predicate condition
     * @param f     a function that evaluates the length of the prefix (a compressed symbol may represents more than one symbol)
     * @return      the length of the prefix
     */
    long deletePrefix(final Predicate<S> pred, final Function<S, Long> f);

    /**
     * Deletes the a suffix of symbols that all satisfy the predicate condition.
     *
     * @param pred  the predicate condition
     * @param f     a function that evaluates the length of the prefix (a compressed symbol may represents more than one symbol)
     * @return      the length of the suffix
     */
    long deleteSuffix(final Predicate<S> pred, final Function<S, Long> f);

    @Override
    IReferencedWord<N, S> clone();

    /**
     * Computes the length of the prefix of the maximal block of a symbols, where a is equal the argument symbol.
     * The length defined as the sum of the lengths of the symbols of the block.
     *
     * @param symbol a symbol
     * @return the length of the prefix of a maximal block of symbol symbols
     */
    long getPrefixLenPrefix(final S symbol);

    /**
     * Computes the length of the suffix of the maximal block of a symbols, where a is equal the argument symbol.
     * The length defined as the sum of the lengths of the symbols of the block.
     *
     * @param symbol a symbol
     * @return the length of the suffix of a maximal block of symbol symbols
     */
    long getSuffixLenPrefix(final S symbol);

    /**
     * Computes the length of the prefix of the maximal block of a symbols and deletes this block from the word, where a is equal the argument symbol.
     * The length defined as the sum of the lengths of the symbols of the block.
     *
     * @param symbol a symbol
     * @return the length of the suffix of a maximal block of symbol symbols
     */
    long deletePrefix(final S symbol);

    /**
     * Computes the length of the suffix of the maximal block of a symbols and deletes this block from the word, where a is equal the argument symbol.
     * The length defined as the sum of the lengths of the symbols of the block.
     *
     * @param symbol a symbol
     * @return the length of the suffix of a maximal block of symbol symbols
     */
    long deleteSuffix(final S symbol);

    /**
     * Deletes all symbols that satisfy the predicate condition.
     *
     * @param pred the predicate condition
     * @return the number of deleted symbols
     */
    int deleteAll(final Predicate<S> pred);

    /**
     * Returns true if there is no symbol satisfying the predicate condition.
     *
     * @param pred the predicate condition
     * @return true if there is no symbol satisfying the predicate condition, otherwise false
     */
    boolean nonMatch(final Predicate<S> pred);

    /**
     * Returns true if all symbols satisfying the predicate condition.
     *
     * @param pred the predicate condition
     * @return true if all symbols satisfying the predicate condition, otherwise false
     */
    boolean allMatch(final Predicate<S> pred);

    @Override
    IReferencedWord<N, S> append(S symbol);
}
