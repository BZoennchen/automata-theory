package grammar.gen;

import data.Node;
import grammar.inter.IReferencedWord;
import symbol.IJezSymbol;
import utils.Pair;

/**
 * This class tests whether at a pointer position is a pair or a block.
 *
 * @author Benedikt Zoennchen
 */
public class ReferencedWordProperties<N, S extends IJezSymbol<N>, W extends IReferencedWord<N, S>> {

    /**
     * Returns true if there is a proper pair equal to pair at the pointer node.
     *
     * @param node  the pointer
     * @param pair  the pair we are interested in
     * @return true if there is a proper pair equal to pair at the pointer node
     */
    public boolean isPairAt(final Node<S> node, final Pair<S, S> pair) {
        return node.hasNext() && node.getElement().equals(pair.a) && node.getNext().getElement().equals(pair.b);
    }

    /**
     * Returns true if there is any proper pair at the pointer position.
     *
     * @param node the pointer
     * @return true if there is any proper pair at the pointer position, otherwise false
     */
    public boolean isPair(final Node<S> node) {
        return node.hasNext() && !node.getElement().equals(node.getNext().getElement())
                && node.getElement().isTerminal() && node.getNext().getElement().isTerminal();
    }

    /**
     * Returns true if there is a crossing pair at the pointer position.
     *
     * @param node the pointer
     * @return true if there is a crossing pair at the pointer position, otherwise false
     */
    public boolean isNonCrossingPair(final Node<S> node) {
        return node.hasNext()
                && node.getElement().isTerminal() && node.getNext().getElement().isTerminal()
                && !node.getElement().equals(node.getNext().getElement());
    }

    /**
     * Test whether there is are two equal terminal symbols at and left to the pointer position. Nevertheless,
     * these symbols can represent two blocks of different length of equal symbols of the last phase.
     *
     * @param node the pointer
     * @return true if there are two equal terminal symbols at and left to the pointer position
     */
    public boolean isNonCompressedBlockAt(final Node<S> node) {
        return node.getElement().isTerminal() && node.hasNext() && node.getElement().equals(node.getNext().getElement());
    }

    /**
     * Test whether there is a symbol at the pointer position representing a block of length > 1.
     *
     * @param node the pointer
     * @return true if there is a symbol at the pointer position representing a block of length > 1, otherwise false
     */
    public boolean isCompressedBlockAt(final Node<S> node) {
        return node.getElement().getLength() > 1 && node.getElement().isTerminal();
    }
}
