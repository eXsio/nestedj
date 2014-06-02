package pl.exsio.nestedj.model;

import java.util.List;

/**
 *
 * @author exsio
 * @param <T>
 */
public interface Tree<T extends NestedNode> {
    
    void setChildren(List<Tree<T>> children);
    
    void addChild(Tree<T> child);
    
    List<Tree<T>> getChildren();
    
    Tree<T> getParent();
    
    void setParent(Tree<T> parent);
    
    T getNode();
    
    void setNode(T node);
    
}
