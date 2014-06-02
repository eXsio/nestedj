package pl.exsio.nestedj.model;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author exsio
 * @param <T>
 */
public class TreeImpl<T extends NestedNode> implements Tree<T> {

    private List<Tree<T>> children;

    private Tree<T> parent;

    private T node;
    
    private TreeImpl() {
        this.children = new LinkedList<Tree<T>>();
    }

    public TreeImpl(T node) {
        this();
        this.node = node;
    }

    public TreeImpl(T node, Tree<T> parent) {
        this();
        this.parent = parent;
        this.node = node;
    }
    
    @Override
    public void addChild(Tree<T> child) {
        this.children.add(child);
        child.setParent(this);
    }

    @Override
    public void setChildren(List<Tree<T>> children) {
        this.children = children;
    }

    @Override
    public List<Tree<T>> getChildren() {
        return this.children;
    }

    @Override
    public Tree<T> getParent() {
        return this.parent;
    }

    @Override
    public void setParent(Tree<T> parent) {
        this.parent = parent;
    }

    @Override
    public T getNode() {
        return this.node;
    }

    @Override
    public void setNode(T node) {
        this.node = node;
    }

}
