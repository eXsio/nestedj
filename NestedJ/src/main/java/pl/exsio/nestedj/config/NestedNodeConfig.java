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
