/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.exsio.nestedj;

import pl.exsio.nestedj.model.NestedNode;

/**
 *
 * @author exsio
 * @param <T>
 */
public interface NestedNodeInserter<T extends NestedNode> extends NestedNodeManipulator<T> {
    
    /**
     *
     * @param node
     * @param parent
     * @param mode
     * @return
     */
    T insert(T node, T parent, int mode);
    
}
