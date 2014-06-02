package pl.exsio.nestedj.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import pl.exsio.nestedj.annotation.LeftColumn;
import pl.exsio.nestedj.annotation.LevelColumn;
import pl.exsio.nestedj.annotation.ParentColumn;
import pl.exsio.nestedj.annotation.RightColumn;

/**
 *
 * @author exsio
 */
@Entity
@Table(name = "nested_nodes")
public class TestNodeImpl implements NestedNode {
           
    /**
     *
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Long id;
    
    /**
     *
     */
    @Column(name = "node_name", nullable = false)
    protected String name;

    /**
     *
     */
    @LeftColumn
    @Column(name = "tree_left", nullable = true)
    protected Long lft;

    /**
     *
     */
    @RightColumn
    @Column(name = "tree_right", nullable = true)
    protected Long rgt;

    /**
     *
     */
    @LevelColumn
    @Column(name = "tree_level", nullable = true)
    protected Long lvl;
 
    /**
     *
     */
    @ManyToOne(fetch = FetchType.EAGER, targetEntity = TestNodeImpl.class)
    @JoinColumn(name = "parent_id", nullable = true)
    @ParentColumn
    protected TestNodeImpl parent;

    /**
     *
     */
    public TestNodeImpl() {
        super();
    }
    
    /**
     *
     * @return
     */
    @Override
    public Long getId() {
        return id;
    }

    /**
     *
     * @return
     */
    @Override
    public Long getLeft() {
        return lft;
    }

    /**
     *
     * @return
     */
    @Override
    public Long getRight() {
        return rgt;
    }


    /**
     *
     * @return
     */
    @Override
    public Long getLevel() {
        return lvl;
    }

    /**
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     * @return
     */
    public TestNodeImpl setName(String name) {
        this.name = name;
        return this;
    }

    /**
     *
     * @return
     */
    @Override
    public TestNodeImpl getParent() {
        return parent;
    }
    

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return "[TestNode id " + this.getId() + ": " + this.getName() +"; left: "+this.getLeft()+", right: "+this.getRight()+", level: "+this.getLevel()+"]";
    }
    
    @Override
    public boolean equals(Object o) {
        if(o instanceof TestNodeImpl) {
            return (this.hashCode() == o.hashCode());
        } else {
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        return this.getId().intValue();
    }

    @Override
    public boolean isRoot() {
        return this.parent == null;
    }
}
