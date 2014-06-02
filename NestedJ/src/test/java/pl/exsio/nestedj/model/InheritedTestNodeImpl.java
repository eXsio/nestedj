/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

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
import pl.exsio.nestedj.annotation.LevelColumn;
import pl.exsio.nestedj.annotation.ParentColumn;
import pl.exsio.nestedj.annotation.RightColumn;

/**
 *
 * @author exsio
 */
@Entity
@Table(name = "inherited_nested_nodes")
public class InheritedTestNodeImpl extends TestNodeImpl {

    
    /**
     *
     */
    @RightColumn
    @Column(name = "inherited_tree_right", nullable = true)
    protected Long inherited_rgt;

    /**
     *
     */
    @LevelColumn
    @Column(name = "inherited_tree_level", nullable = true)
    protected Long inherited_lvl;
    
}
