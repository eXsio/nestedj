package pl.exsio.nestedj;

import pl.exsio.nestedj.ex.InvalidNodesHierarchyException;
import pl.exsio.nestedj.model.NestedNode;

/**
 *
 * @author exsio
 * @param <T>
 */
public interface NestedNodeMover<T extends NestedNode> extends NestedNodeHierarchyManipulator<T> {
 
    T move(T node, T parent, int mode) throws InvalidNodesHierarchyException;
}
