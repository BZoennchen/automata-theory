package morphismEq;

import morphismEq.edges.IGrammarEdge;
import symbol.NaturalSymbol;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author Benedikt Zoennchen
 */
public class Path<S extends NaturalSymbol> implements Iterable<IGrammarEdge<S>>{
    private LinkedList<IGrammarEdge<S>> stack;

    public Path() {
        stack = new LinkedList<>();
    }

    public void addEdge(final IGrammarEdge<S> edge) {
        this.stack.add(edge);
    }

    public IGrammarEdge<S> removeLast() {
        return this.stack.removeLast();
    }

    public S tail() {
        return stack.peekLast().getEnd();
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public int length() {
        return stack.size();
    }

    @Override
    public Iterator<IGrammarEdge<S>> iterator() {
        return stack.iterator();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        stack.forEach(edge -> builder.append(edge));
        builder.append("]");
        return builder.toString();
    }
}