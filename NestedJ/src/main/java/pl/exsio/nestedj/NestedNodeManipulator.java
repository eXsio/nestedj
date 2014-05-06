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
 */
public interface NestedNodeManipulator<T extends NestedNode> {
    
    /**
     *
     */
    int MODE_FIRST_CHILD = 0;

    /**
     *
     */
    int MODE_LAST_CHILD = 1;

    /**
     *
     */
    int MODE_NEXT_SIBLING = 2;

    /**
     *
     */
    int MODE_PREV_SIBLING = 3;
   
}
