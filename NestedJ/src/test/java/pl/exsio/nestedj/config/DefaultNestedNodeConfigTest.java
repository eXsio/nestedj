/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.exsio.nestedj.config;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author exsio
 */
public class DefaultNestedNodeConfigTest {
    
    public DefaultNestedNodeConfigTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getEntityName method, of class DefaultNestedNodeConfig.
     */
    @Test
    public void testGetEntityName() {
        System.out.println("getEntityName");
        DefaultNestedNodeConfig instance = new DefaultNestedNodeConfig();
        String expResult = null;
        String result = instance.getEntityName();
        assertEquals(expResult, result);
    }

    /**
     * Test of setEntityName method, of class DefaultNestedNodeConfig.
     */
    @Test
    public void testSetEntityName() {
        System.out.println("setEntityName");
        String entityName = null;
        DefaultNestedNodeConfig instance = new DefaultNestedNodeConfig();
        instance.setEntityName(entityName);
    }

    /**
     * Test of getRightFieldName method, of class DefaultNestedNodeConfig.
     */
    @Test
    public void testGetRightFieldName() {
        System.out.println("getRightFieldName");
        DefaultNestedNodeConfig instance = new DefaultNestedNodeConfig();
        String expResult = null;
        String result = instance.getRightFieldName();
        assertEquals(expResult, result);
    }

    /**
     * Test of setRightFieldName method, of class DefaultNestedNodeConfig.
     */
    @Test
    public void testSetRightFieldName() {
        System.out.println("setRightFieldName");
        String rightFieldName = null;
        DefaultNestedNodeConfig instance = new DefaultNestedNodeConfig();
        instance.setRightFieldName(rightFieldName);
    }

    /**
     * Test of getLeftFieldName method, of class DefaultNestedNodeConfig.
     */
    @Test
    public void testGetLeftFieldName() {
        System.out.println("getLeftFieldName");
        DefaultNestedNodeConfig instance = new DefaultNestedNodeConfig();
        String expResult = null;
        String result = instance.getLeftFieldName();
        assertEquals(expResult, result);
    }

    /**
     * Test of setLeftFieldName method, of class DefaultNestedNodeConfig.
     */
    @Test
    public void testSetLeftFieldName() {
        System.out.println("setLeftFieldName");
        String leftFieldName = null;
        DefaultNestedNodeConfig instance = new DefaultNestedNodeConfig();
        instance.setLeftFieldName(leftFieldName);
    }

    /**
     * Test of getLevelFieldName method, of class DefaultNestedNodeConfig.
     */
    @Test
    public void testGetLevelFieldName() {
        System.out.println("getLevelFieldName");
        DefaultNestedNodeConfig instance = new DefaultNestedNodeConfig();
        String expResult = null;
        String result = instance.getLevelFieldName();
        assertEquals(expResult, result);
    }

    /**
     * Test of setLevelFieldName method, of class DefaultNestedNodeConfig.
     */
    @Test
    public void testSetLevelFieldName() {
        System.out.println("setLevelFieldName");
        String levelFieldName = null;
        DefaultNestedNodeConfig instance = new DefaultNestedNodeConfig();
        instance.setLevelFieldName(levelFieldName);
    }

    /**
     * Test of getParentFieldName method, of class DefaultNestedNodeConfig.
     */
    @Test
    public void testGetParentFieldName() {
        System.out.println("getParentFieldName");
        DefaultNestedNodeConfig instance = new DefaultNestedNodeConfig();
        String expResult = null;
        String result = instance.getParentFieldName();
        assertEquals(expResult, result);
    }

    /**
     * Test of setParentFieldName method, of class DefaultNestedNodeConfig.
     */
    @Test
    public void testSetParentFieldName() {
        System.out.println("setParentFieldName");
        String parentFieldName = null;
        DefaultNestedNodeConfig instance = new DefaultNestedNodeConfig();
        instance.setParentFieldName(parentFieldName);
    }

    /**
     * Test of toString method, of class DefaultNestedNodeConfig.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        DefaultNestedNodeConfig instance = new DefaultNestedNodeConfig();
        String expResult = "[leftFieldName: null, rightFieldName:null, levelFieldName: null, parentFieldName:null]";
        String result = instance.toString();
        assertEquals(expResult, result);
    }
    
}
