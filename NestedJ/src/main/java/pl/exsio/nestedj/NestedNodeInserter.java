package pl.exsio.nestedj;

import pl.exsio.nestedj.model.NestedNode;

/**
 *
 * @author exsio
 * @param <T>
 */
public interface NestedNodeInserter<T extends NestedNode> extends NestedNodeHierarchyManipulator<T> {
    
    T insert(T node, T parent, int mode);
    
}
