package morphismEq.edges;

import data.MyLinkedList;
import data.Node;
import symbol.NaturalSymbol;

/**
 * A IGrammarEdge represents an edge of the grammar graph from a non-terminal X to a non-terminal Y
 * i.e. (X, Y). The edge represents a production X -> Ya or X -> aY or X -> ab. The edge is able to append
 * the words a or ab to the list of words generated by an Plandowski path.
 *
 * furthermore they are able to append the words
 *
 * @author Benedikt Zoennchen
 */
public interface IGrammarEdge<S extends NaturalSymbol> {
    void append(final MyLinkedList<S> list, final Node<S> anchor);

    /**
     * The source node i.e. source non-terminal.
     *
     * @return the source node
     */
    S getStart();

    /**
     * The destination node i.e. destination non-terminal or the special terminal node T.
     *
     * @return the destination node
     */
    S getEnd();


}
