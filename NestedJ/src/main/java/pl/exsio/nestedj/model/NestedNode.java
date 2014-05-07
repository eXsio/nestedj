/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.exsio.nestedj.model;

/**
 *
 * @author exsio
 */
public interface NestedNode {
    
    /**
     *
     * @return
     */
    Long getId();
    
    /**
     *
     * @return
     */
    Long getLeft();

    /**
     *
     * @return
     */
    Long getRight();

    /**
     *
     * @return
     */
    Long getLevel();

    /**
     *
     * @return
     */
    NestedNode getParent();
    
    /**
     * 
     * @return 
     */
    boolean isRoot();
    

}
