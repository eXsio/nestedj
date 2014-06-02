package pl.exsio.nestedj;

import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.model.Tree;

/**
 *
 * @author exsio
 * @param <T>
 */
public interface NestedNodeRetriever<T extends NestedNode> {
    
    Iterable<T> getTreeAsList(T node);
    
    Iterable<T> getChildren(T node);
    
    T getParent(T node);
    
    Tree<T> getTree(T node);
    
    Iterable<T> getParents(T node);
}
