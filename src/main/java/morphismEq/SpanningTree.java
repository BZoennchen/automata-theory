package morphismEq;

import morphismEq.edges.IGrammarEdge;
import symbol.NaturalSymbol;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Benedikt Zoennchen
 */
public class SpanningTree<S extends NaturalSymbol> implements Iterable<Path<S>>{
    private TreeNode<S> root;
    private List<TreeNode<S>> nodes;
    private final IGrammarEdge<S>[][] adjazenzmatrix;

    public SpanningTree(final TreeNode<S> root, final List<TreeNode<S>> nodes, final IGrammarEdge<S>[][] adjazenzmatrix) {
        this.root = root;
        this.adjazenzmatrix = adjazenzmatrix;
        this.nodes = nodes;
    }

    public int height() {
        return 0;
    }

    public TreeNode<S> getRoot() {
        return root;
    }

    public LinkedList<IGrammarEdge<S>> getPath(final int targetNodeId, LinkedList<IGrammarEdge<S>> head) {

        if(targetNodeId != root.get().getId()) {
            TreeNode<S> current = root;

            for(TreeNode<S> neighbour : current.getNeighbours()) {
                head.add(getEdge(current, neighbour));
                head = getPath(targetNodeId, neighbour, head);
                if(head.getLast().getEnd().getId() == targetNodeId) {
                    return head;
                }
                head.removeLast();
            }
        }

        return head;
    }

    private LinkedList<IGrammarEdge<S>> getPath(final int targetNodeId, final TreeNode<S> current, LinkedList<IGrammarEdge<S>> path) {
        if(current.get().getId() == targetNodeId) {
            return path;
        }

        for(TreeNode<S> neighbour : current.getNeighbours()) {
            path.add(getEdge(current, neighbour));
            path = getPath(targetNodeId, neighbour, path);
            if(path.getLast().getEnd().getId() == targetNodeId) {
                return path;
            }
            path.removeLast();
        }

        return path;
    }

    private IGrammarEdge<S> getEdge(final TreeNode<S> source, final TreeNode<S> target) {
        return adjazenzmatrix[source.get().getId()][target.get().getId()];
    }

    public List<TreeNode<S>> nodes() {
        return nodes;
    }


    @Override
    public Iterator<Path<S>> iterator() {
        return new PathIterator();
    }

    /**
     * Iterates over all Path of the tree in a dept-first fashion.
     */
    private class PathIterator implements Iterator<Path<S>> {

        private Path path;
        private LinkedList<TreeNode<S>> treePath;
        private LinkedList<Iterator<TreeNode<S>>> stack;
        private boolean showedEmptyPath;

        private PathIterator() {
            treePath = new LinkedList<>();
            treePath.add(root);
            stack = new LinkedList<>();
            if(root.hasNeighbours()) {
                stack.add(root.getNeighbours().iterator());
            }

            path = new Path();
            showedEmptyPath = false;
        }

        @Override
        public boolean hasNext() {
            if(!showedEmptyPath) {
                return true;
            }


            if(stack.isEmpty()) {
                return false;
            }
            else if(!stack.isEmpty() && stack.peekLast().hasNext()) {
                return true;
            }

            if(!stack.isEmpty()){
                clearStack();
            }

            return hasNext();
        }

        private void clearStack() {
            if(!stack.isEmpty()){
                while (!stack.isEmpty() && !stack.peekLast().hasNext()) {
                    treePath.removeLast();
                    stack.removeLast();
                    if(stack.size() > 0) {
                        path.removeLast();
                    }
                }
            }
        }

        @Override
        public Path next() {
            if(!showedEmptyPath) {
                showedEmptyPath = true;
                return new Path();
            }


            if(stack.isEmpty()) {
                return null;
            }
            // build the path
            TreeNode<S> current = treePath.peekLast();
            Iterator<TreeNode<S>> neighbours = stack.peekLast();

            // go deeper
            if(neighbours.hasNext()) {
                TreeNode<S> neighbour = neighbours.next();
                stack.add(neighbour.getNeighbours().iterator());
                path.addEdge(adjazenzmatrix[current.get().getId()][neighbour.get().getId()]);
                treePath.add(neighbour);
                return path;
            }
            else {
                clearStack();
                return next();
            }
        }
    }

}
