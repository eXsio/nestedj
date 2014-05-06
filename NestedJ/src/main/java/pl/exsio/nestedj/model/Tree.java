/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

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
