package pl.exsio.nestedj.dao;

import pl.exsio.nestedj.ex.InvalidNodesHierarchyException;
import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.model.Tree;

/**
 *
 * @author exsio
 * @param <T>
 */
public interface NestedNodeDao<T extends NestedNode> {

    T insertAsFirstChildOf(T node, T parent) throws InvalidNodesHierarchyException;

    T insertAsLastChildOf(T node, T parent) throws InvalidNodesHierarchyException;

    T insertAsNextSiblingOf(T node, T parent) throws InvalidNodesHierarchyException;

    T insertAsPrevSiblingOf(T node, T parent) throws InvalidNodesHierarchyException;

    void removeSingle(T node);

    void removeSubtree(T node);    

    Iterable<T> getChildren(T node);

     T getParent(T node);

     Iterable<T> getParents(T node);

    Iterable<T> getTreeAsList(T node);

    Tree<T> getTree(T node);

    void rebuildTree(T node);
}
