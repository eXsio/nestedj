/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.exsio.nestedj.dao;

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
     */
    T insertAsFirstChildOf(T node, T parent);

    /**
     *
     * @param node
     * @param parent
     * @return
     */
    T insertAsLastChildOf(T node, T parent);

    /**
     *
     * @param node
     * @param parent
     * @return
     */
    T insertAsNextSiblingOf(T node, T parent);

    /**
     *
     * @param node
     * @param parent
     * @return
     */
    T insertAsPrevSiblingOf(T node, T parent);
    
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
    
   
    Tree<T> getTree(T node);
}
