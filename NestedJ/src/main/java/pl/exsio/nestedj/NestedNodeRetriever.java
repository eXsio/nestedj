/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

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
}
