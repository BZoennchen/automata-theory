package morphismEq.edges;

import symbol.NaturalSymbol;

/**
 * @author Benedikt Zoennchen
 */
public abstract class AbstractGrammarEdge<S extends NaturalSymbol> implements IGrammarEdge<S> {
    private S startVertex;
    private S endVertex;

    public AbstractGrammarEdge(final S startVertex, final S endVertex) {
        this.startVertex = startVertex;
        this.endVertex = endVertex;
    }

    @Override
    public S getStart() {
        return startVertex;
    }

    @Override
    public S getEnd() {
        return endVertex;
    }

    @Override
    public String toString() {
        return "("+startVertex+", "+ endVertex+")";
    }

}