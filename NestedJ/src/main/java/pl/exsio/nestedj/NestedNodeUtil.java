/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.exsio.nestedj;

import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.config.NestedNodeConfig;
import pl.exsio.nestedj.model.Tree;

/**
 *
 * @author exsio
 * @param <T>
 */
public interface NestedNodeUtil<T extends NestedNode> {

    /**
     *
     * @param node
     * @return
     */
    boolean isNodeNew(T node);
    
    /**
     * 
     * @param nodeClass
     * @return 
     */
    NestedNodeConfig getNodeConfig(Class<? extends NestedNode> nodeClass);
    
}
