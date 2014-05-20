/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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

    /**
     *
     * @param node
     * @param parent
     * @return
     * @throws pl.exsio.nestedj.ex.InvalidNodesHierarchyException
     */
    T insertAsFirstChildOf(T node, T parent) throws InvalidNodesHierarchyException;

    /**
     *
     * @param node
     * @param parent
     * @return
     * @throws pl.exsio.nestedj.ex.InvalidNodesHierarchyException
     */
    T insertAsLastChildOf(T node, T parent) throws InvalidNodesHierarchyException;

    /**
     *
     * @param node
     * @param parent
     * @return
     * @throws pl.exsio.nestedj.ex.InvalidNodesHierarchyException
     */
    T insertAsNextSiblingOf(T node, T parent) throws InvalidNodesHierarchyException;

    /**
     *
     * @param node
     * @param parent
     * @return
     * @throws pl.exsio.nestedj.ex.InvalidNodesHierarchyException
     */
    T insertAsPrevSiblingOf(T node, T parent) throws InvalidNodesHierarchyException;
    
    /**
     * 
     * @param node 
     */
    void removeSingle(T node);
    
    /**
     * 
     * @param node 
     */
    void removeSubtree(T node);    
    

    /**
     *
     * @param node
     * @return
     */
    Iterable<T> getChildren(T node);
    
    /**
     * 
     * @param node
     * @return 
     */
     T getParent(T node);
     
     /**
     * 
     * @param node
     * @return 
     */
     Iterable<T> getParents(T node);
    
    /**
     * 
     * @param node
     * @return 
     */
    Iterable<T> getTreeAsList(T node);
    
   /**
    * 
    * @param node
    * @return 
    */
    Tree<T> getTree(T node);
    
    /**
     * 
     * @param node 
     */
    void rebuildTree(T node);
}
