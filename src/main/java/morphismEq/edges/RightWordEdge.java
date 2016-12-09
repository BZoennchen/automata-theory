package morphismEq.edges;

import data.MyLinkedList;
import data.Node;
import symbol.NaturalSymbol;

/**
 * Edge representing a production of the form X -> Ya,  where a is a non-terminal words represented by SLPs.
 *
 * @author Benedikt Zoennchen
 */
public class RightWordEdge<S extends NaturalSymbol> extends AbstractGrammarEdge<S> {

    private final S word;

    public RightWordEdge(final S startVertex, final S endVertex, final S word) {
        super(startVertex, endVertex);
        this.word = word;
    }

    @Override
    public void append(MyLinkedList<S> list, Node<S> anchor) {
        list.insertNext(word, anchor);
    }
}