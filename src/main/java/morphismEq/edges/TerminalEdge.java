package morphismEq.edges;

import data.MyLinkedList;
import data.Node;
import symbol.NaturalSymbol;

/**
 * Edge representing a production of the form X -> a, where a is a normal terminal.
 *
 * @author Benedikt Zoennchen
 */
public class TerminalEdge<S extends NaturalSymbol> extends AbstractGrammarEdge<S> {

    private final S terminal;

    public TerminalEdge(final S startVertex, final S endVertex, final S terminal) {
        super(startVertex, endVertex);
        this.terminal = terminal;
    }

    @Override
    public void append(MyLinkedList<S> list, Node<S> anchor) {
        list.insertNext(terminal, anchor);
    }
}
