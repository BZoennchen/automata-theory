package morphismEq;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Benedikt Zoennchen
 */
public class TreeNode<S> {
    private List<TreeNode<S>> neighbours;
    private S element;

    public TreeNode(final S element) {
        this.element = element;
        this.neighbours = new ArrayList<>();
    }

    public S get() {
        return element;
    }

    public List<TreeNode<S>> getNeighbours() {
        return neighbours;
    }

    public boolean hasNeighbours() {
        return !neighbours.isEmpty();
    }

    public void addNeighbour(final TreeNode<S> treeNode) {
        this.neighbours.add(treeNode);
    }
}