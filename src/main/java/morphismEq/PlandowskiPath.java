package morphismEq;

import morphismEq.edges.IGrammarEdge;
import symbol.NaturalSymbol;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author Benedikt Zoennchen
 */
public class PlandowskiPath<S extends NaturalSymbol> implements Iterable<IGrammarEdge<S>>{

    private LinkedList<Path<S>> treePaths;
    private Path<S> edges;

    public PlandowskiPath() {
        this.treePaths = new LinkedList<>();
        this.edges = new Path();
    }

    public void addEdges(final IGrammarEdge<S> edge) {
        if(edges.length()+1 == treePaths.size()) {
            this.edges.addEdge(edge);
        }
        else {
            throw new IllegalArgumentException("wrong order in plandowski tree, a tree-path is missing.");
        }
    }

    public void addTreePath(final Path path) {
        if(edges.length() == treePaths.size()) {
            this.treePaths.add(path);
        } else {
            throw new IllegalArgumentException("wrong order in plandowski tree, an edge is missing.");
        }
    }

    public IGrammarEdge<S> removeLastEdge() {
        return this.edges.removeLast();
    }

    public Path removeLastTreePath() {
        return treePaths.removeLast();
    }

    public S tail() {
        if(edges.length() == treePaths.size()) {
            return edges.tail();
        }
        else if(edges.length() + 1 == treePaths.size()) {
            return treePaths.peekLast().tail();
        }
        else {
            throw new RuntimeException("illegal plandowski tree structure.");
        }
    }

    public boolean isEmpty() {
        return treePaths.isEmpty() && edges.isEmpty();
    }

    public int length() {
        return edges.length();
    }

    @Override
    public Iterator<IGrammarEdge<S>> iterator() {
        return new EdgeIterator();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        Iterator<Path<S>> itTree = treePaths.iterator();
        Iterator<IGrammarEdge<S>> itEdges = edges.iterator();

        while (itTree.hasNext()) {
            builder.append(itTree.next());
            if(itEdges.hasNext()) {
                builder.append(itEdges.next());
            }
        }
        return builder.toString();
    }

    public String toStringPlandowskiEdges() {
        StringBuilder builder = new StringBuilder();
        Iterator<IGrammarEdge<S>> itEdges = edges.iterator();

        while (itEdges.hasNext()) {
            builder.append(itEdges.next());
        }
        return builder.toString();
    }

    private class EdgeIterator implements Iterator<IGrammarEdge<S>> {

        private Iterator<Path<S>> itTrees;
        private Iterator<IGrammarEdge<S>> itCurrentTree;
        private Iterator<IGrammarEdge<S>> itEdges;
        private boolean handledTree = false;

        private EdgeIterator() {
            itTrees = treePaths.iterator();
            itEdges = edges.iterator();
            itCurrentTree = itTrees.hasNext() ? itTrees.next().iterator() : null;
        }

        @Override
        public boolean hasNext() {

            // handle an edge
            if(handledTree) {
                return itEdges.hasNext();
            } // handle a tree
            else {
                // the current tree is not jet completed
                if(itCurrentTree == null) {
                    return false;
                }
                else {
                    if(!itCurrentTree.hasNext()) {
                        return itEdges.hasNext();
                    }
                    else {
                        return itCurrentTree.hasNext();
                    }
                }
            }
        }

        @Override
        public IGrammarEdge<S> next() {
            if(handledTree) {
                handledTree = false;

                if(itTrees.hasNext()) {
                    itCurrentTree = itTrees.next().iterator();
                }

                return itEdges.next();
            }
            else {
                IGrammarEdge<S> next = null;
                if(itCurrentTree.hasNext()) {
                    next = itCurrentTree.next();
                    handledTree = !itCurrentTree.hasNext();
                }
                else {
                    handledTree = true;
                    next = next();
                }
                return next;
            }
        }
    }
}