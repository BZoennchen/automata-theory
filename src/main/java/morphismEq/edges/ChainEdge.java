package morphismEq.edges;

import data.MyLinkedList;
import data.Node;
import symbol.NaturalSymbol;

/**
 * Edge representing a production of the form X -> Y.
 *
 * @author Benedikt Zoennchen
 */
public class ChainEdge<S extends NaturalSymbol> extends AbstractGrammarEdge<S> {

    public ChainEdge(final S startVertex, final S endVertex) {
        super(startVertex, endVertex);
    }

    @Override
    public void append(final MyLinkedList<S> list, final Node<S> anchor) {}
}
