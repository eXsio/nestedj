package pl.exsio.nestedj;

import pl.exsio.nestedj.ex.InvalidNodesHierarchyException;
import pl.exsio.nestedj.model.NestedNode;

/**
 *
 * @author exsio
 * @param <T>
 */
public interface NestedNodeRebuilder<T extends NestedNode> {
     
    void rebuildTree(Class<? extends NestedNode> nodeClass) throws InvalidNodesHierarchyException;
}
