package pl.exsio.nestedj.model;

/**
 *
 * @author exsio
 */
public interface NestedNode {

    Long getId();

    Long getLeft();

    Long getRight();

    Long getLevel();

    NestedNode getParent();

    boolean isRoot();
    
}
