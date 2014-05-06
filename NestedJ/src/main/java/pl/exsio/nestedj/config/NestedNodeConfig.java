/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.exsio.nestedj.config;

/**
 *
 * @author exsio
 */
public interface NestedNodeConfig {
    
    String getEntityName();
    
    void setEntityName(String name);
    
    String getRightFieldName();
    
    void setRightFieldName(String name);
    
    String getLeftFieldName();
    
    void setLeftFieldName(String name);
    
    String getLevelFieldName();
    
    void setLevelFieldName(String name);
    
    String getParentFieldName();
    
    void setParentFieldName(String name);
    
}
