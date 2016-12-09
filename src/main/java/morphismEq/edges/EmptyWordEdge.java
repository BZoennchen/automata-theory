package morphismEq.edges;

import data.MyLinkedList;
import data.Node;
import symbol.NaturalSymbol;

/**
 * Edge representing a production of the form X -> epsilon.
 *
 * @author Benedikt Zoennchen
 */
public class EmptyWordEdge<S extends NaturalSymbol> extends AbstractGrammarEdge<S> {

    public EmptyWordEdge(S startVertex, S endVertex) {
        super(startVertex, endVertex);
    }

    @Override
    public void append(MyLinkedList<S> list, Node<S> anchor) {}
}


