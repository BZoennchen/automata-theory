package grammar.gen;

import data.MyLinkedList;
import data.Node;
import grammar.inter.IReferencedWord;
import grammar.inter.IWord;
import symbol.IJezSymbol;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A GenPointedWord is a generic implementation IPointableWord and represents a word and we use it as
 * the right-hand side of a productions for which one can extract pointers to its symbols.
 *
 * @author Benedikt Zoennchen
 *
 * @param <N>   the type of the identifier of terminal and non-terminal symbols of the grammar
 * @param <S>   the type of the terminal and non-terminal symbols of the grammar
 */
public class GenPointedWord<N, S extends IJezSymbol<N>> implements IReferencedWord<N, S> {

    /**
     * pointers to all symbols of the right-hand side.
     */
    private MyLinkedList<S> nodes;

    /**
     * The default constructor of a generic word, containing one symbol.
     *
     * @param symbol the symbol contained in this word
     */
    public GenPointedWord(final S symbol) {
        this.nodes = new MyLinkedList<>();
        this.nodes.add(symbol);
    }

    /**
     * Construct a word containing all symbols in order.
     *
     * @param symbols a list of symbols
     */
    public GenPointedWord(final List<S> symbols) {
        this.nodes = new MyLinkedList<>(symbols);
    }

    /**
     * Construct a word containing all symbols contained in nodes.
     * Furthermore the word uses the pointers nodes.
     *
     * @param nodes a list of symbols
     */
    public GenPointedWord(final MyLinkedList<S> nodes) {
        this.nodes = nodes;
    }

    @Override
    public long getPrefixLenPrefix(final S letter) {
        return nodes.prefixLen(l -> l.equals(letter), l -> l.getLength());
    }

    @Override
    public long getSuffixLenPrefix(final S letter) {
        return nodes.suffixLen(l -> l.equals(letter), l -> l.getLength());
    }

    /**
     * Deletes a prefix where all letters are equals letter. Complexity: O(|rhs|).
     * @param symbol the letter that defines the prefix
     * @return the length of the prefix
     */
    @Override
    public long deletePrefix(final S symbol) {
        return nodes.removePrefix(l -> l.equals(symbol), l -> l.getLength());
    }

    /**
     * Deletes a suffix where all letters are the same. Complexity: O(|rhs|).
     * @param symbol the letter that defines the suffix
     * @return the length of the suffix
     */
    @Override
    public long deleteSuffix(final S symbol) {
        return nodes.removeSuffix(l -> l.equals(symbol), l -> l.getLength());
    }

    @Override
    public int deleteAll(final Predicate<S> pred) {
        return nodes.removeAll(pred);
    }

    @Override
    public boolean nonMatch(final Predicate<S> pred) {
        return this.stream().noneMatch(pred);
    }

    @Override
    public boolean allMatch(Predicate<S> pred) {
        return this.stream().allMatch(pred);
    }

    @Override
    public Stream<Node<S>> nodeStream() {
        return nodes.stream();
    }

    @Override
    public Iterator<Node<S>> nodeIterator() {
        return nodes.iterator();
    }

    @Override
    public List<Node<S>> findAllPointers(final Predicate<S> pred) {
        return nodeStream().filter(node -> pred.test(node.getElement())).collect(Collectors.toList());
    }

    @Override
    public S get(int i) {
        return nodes.get(i).getElement();
    }

    @Override
    public Stream<S> stream() {
        return nodes.stream().map(node -> node.getElement());
    }

    @Override
    public Iterator<S> iterator() {
        return nodes.elementIterator();
    }

    @Override
    public IReferencedWord<N, S> clone() {
        return new GenPointedWord<>(nodes.clone());
    }

    @Override
    public List<S> findAll(final Predicate<S> pred) {
        return stream().filter(symbol -> pred.test(symbol)).collect(Collectors.toList());
    }

    @Override
    public List<S> toList() {
        return findAll(s -> true);
    }

    @Override
    public List<Node<S>> findAllNodes(Predicate<S> pred) {
        return nodeStream().filter(node -> pred.test(node.getElement())).collect(Collectors.toList());
    }

    @Override
    public Optional<S> findLast(final Predicate<S> pred) {
        Optional<Node<S>> optNode = nodes.findLast(pred);
        if(optNode.isPresent()) {
            return Optional.of(optNode.get().getElement());
        }
        else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<S> findFirst(Predicate<S> pred) {
        Optional<Node<S>> optNode = nodes.findFirst(pred);
        if(optNode.isPresent()) {
            return Optional.of(optNode.get().getElement());
        }
        else {
            return Optional.empty();
        }
    }

    @Override
    public List<List<S>> split(final Predicate<S> predicate) {
        MyLinkedList<S> splitNodes = nodes;
        List<List<S>> list = new LinkedList<>();

        MyLinkedList<S> splitLetters = splitNodes.split(predicate);
        while (!splitLetters.isEmpty()) {
            list.add(splitLetters.toList());
            splitLetters = splitNodes.split(predicate);
        }

        // add the rest
        list.add(splitNodes.toList());
        return list;
    }

    @Override
    public IWord<S> concat(IWord<S> word) {
        MyLinkedList<S> nodes = this.nodes;
        nodes.append(word.stream().collect(Collectors.toList()));
        return this;
    }

    @Override
    public IReferencedWord<N, S> append(S symbol) {
        MyLinkedList<S> nodes = this.nodes;
        nodes.addLast(symbol);
        return this;
    }

    @Override
    public IWord<S> suspend(S symbol) {
        MyLinkedList<S> nodes = this.nodes;
        nodes.addFirst(symbol);
        return this;
    }

    @Override
     public IWord<S> deleteLast() {
        if(!nodes.isEmpty()) {
            nodes.removeTail();
        }
        return this;
    }

    @Override
    public IWord<S> deleteFirst() {
        if(!nodes.isEmpty()) {
            nodes.removeHead();
        }
        return this;
    }

    @Override
    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    @Override
    public boolean isSingleton() {
        return !nodes.isEmpty() && nodes.getHead().getNext() == null;
    }

    @Override
    public int length() {
        return nodes.size();
    }

    @Override
    public S getFirst() {
        if(!nodes.isEmpty()) {
            return nodes.getHead().getElement();
        }
        return null;
    }

    @Override
    public S getLast() {
        if(!nodes.isEmpty()) {
            return nodes.getTail().getElement();
        }
        return null;
    }

    @Override
    public long deletePrefix(final Predicate<S> pred, final Function<S, Long> f) {
        return nodes.removePrefix(pred, f);
    }

    @Override
    public long deleteSuffix(Predicate<S> pred, final Function<S, Long> f) {
        return nodes.removeSuffix(pred, f);
    }

    @Override
    public String toString() {
        return nodes.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof GenPointedWord)) return false;

        GenPointedWord<N,S> that = (GenPointedWord<N, S>) o;

        return !(nodes != null ? !nodes.equals(that.nodes) : that.nodes != null);

    }

    @Override
    public int hashCode() {
        return nodes != null ? nodes.hashCode() : 0;
    }
}
