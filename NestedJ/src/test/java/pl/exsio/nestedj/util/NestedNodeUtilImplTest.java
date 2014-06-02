/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.exsio.nestedj.util;

import static org.junit.Assert.*;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.exsio.nestedj.FunctionalNestedjTest;
import pl.exsio.nestedj.NestedNodeUtil;
import pl.exsio.nestedj.config.NestedNodeConfig;
import pl.exsio.nestedj.model.InheritedTestNodeImpl;
import pl.exsio.nestedj.model.TestNodeImpl;

/**
 *
 * @author exsio
 */
public class NestedNodeUtilImplTest extends FunctionalNestedjTest {
    
    @Autowired
    private NestedNodeUtil util;
    
    public NestedNodeUtilImplTest() {
    }

    @Test
    public void testIsNodeNew() {

        TestNodeImpl node = new TestNodeImpl();
        NestedNodeUtilImpl instance = new NestedNodeUtilImpl();
        boolean expResult = true;
        boolean result = instance.isNodeNew(node);
        assertEquals(expResult, result);
    }

    @Test
    public void testGetNodeConfigWithoutInheritence() {
        
        NestedNodeConfig config = this.util.getNodeConfig(TestNodeImpl.class);
        assertEquals(config.getEntityName(), "TestNodeImpl");
        assertEquals(config.getLeftFieldName(), "lft");
        assertEquals(config.getRightFieldName(), "rgt");
        assertEquals(config.getLevelFieldName(), "lvl");
        assertEquals(config.getParentFieldName(), "parent");
        
    }
    
    @Test
    public void testGetNodeConfigWithInheritence() {
        
        NestedNodeConfig config = this.util.getNodeConfig(InheritedTestNodeImpl.class);
        assertEquals(config.getEntityName(), "InheritedTestNodeImpl");
        assertEquals(config.getRightFieldName(), "inherited_rgt");
        assertEquals(config.getLevelFieldName(), "inherited_lvl");
        assertEquals(config.getLeftFieldName(), "lft");
        assertEquals(config.getParentFieldName(), "parent");
        
    }
    
}
