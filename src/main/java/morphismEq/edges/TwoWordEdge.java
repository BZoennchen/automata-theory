package morphismEq.edges;

import data.MyLinkedList;
import data.Node;
import symbol.NaturalSymbol;

/**
 * Edge representing a production of the form X -> ab, where a and b are non-terminal words represented by SLPs.
 *
 * @author Benedikt Zoennchen
 */
public class TwoWordEdge<S extends NaturalSymbol> extends AbstractGrammarEdge<S> {
    private final S leftWord;
    private final S rightWord;

    public TwoWordEdge(final S startVertex, final S endVertex, final S leftWord, final S rightWord) {
        super(startVertex, endVertex);
        this.leftWord = leftWord;
        this.rightWord = rightWord;
    }

    @Override
    public void append(final MyLinkedList<S> list, final Node<S> anchor) {
        list.insertPrevious(leftWord, anchor);
        list.insertNext(rightWord, anchor);
    }
}
