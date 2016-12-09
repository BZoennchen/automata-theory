package DFA.gen;

import java.util.LinkedList;
import java.util.List;

/**
 * A generic node of a tree. A tree is defined by a node and all its children.
 * The node knows its parent (if its not the root) and its children.
 * We use this data structure to support the FIND and UNION operation used in the
 * equality test for deterministic word automaton.
 *
 * @author Benedikt Zoennchen
 *
 * @param <E> the type of the element of this generic node
 */
public class GenNode<E> {

    /**
     * The size of the sub tree rooted at this node.
     */
    private int size;

    /**
     * The element of the node.
     */
    private E element;

    /**
     * The parent of the node.
     */
    private GenNode<E> parent;

    /**
     * The child nodes of the node.
     */
    private List<GenNode<E>> children;

    /**
     * Default constructor, create a new single node containing a element.
     *
     * @param element   the element of this node.
     */
    public GenNode(final E element) {
        this.element = element;
        this.size = 1;
        this.children = new LinkedList<>();
    }

    /**
     * Appends a new child to this node.
     *
     * @param node the new child
     */
    public void append(final GenNode<E> node) {
        this.children.add(node);
        node.setParent(this);
        this.size += node.size;
    }

    /**
     * Returns the root of the tree of this node.
     *
     * @return the root of the tree of this node
     */
    public GenNode<E> getRoot() {
        GenNode<E> root = this;
        while(root.parent != null) {
            root = root.parent;
        }

        return root;
    }

    /**
     * Returns the element of this node.
     *
     * @return the element of this node
     */
    public E getElement() {
        return element;
    }

    /**
     * Returns the size of the subtree rooted at this node.
     *
     * @return the size of the subtree rooted at this node
     */
    public int getSize() {
        return size;
    }

    /**
     * Sets the parent of this node. Therefore the tree rooted
     * at this node will be rooted at the new parent.
     *
     * @param parent the new parent of this node
     */
    public void setParent(final GenNode<E> parent) {
        this.parent = parent;
    }
}
