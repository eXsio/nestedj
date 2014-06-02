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
     * Test of getEntityName method, of class NestedNodeConfigImpl.
     */
    @Test
    public void testGetEntityName() {
        System.out.println("getEntityName");
        NestedNodeConfigImpl instance = new NestedNodeConfigImpl();
        String expResult = null;
        String result = instance.getEntityName();
        assertEquals(expResult, result);
    }

    /**
     * Test of setEntityName method, of class NestedNodeConfigImpl.
     */
    @Test
    public void testSetEntityName() {
        System.out.println("setEntityName");
        String entityName = null;
        NestedNodeConfigImpl instance = new NestedNodeConfigImpl();
        instance.setEntityName(entityName);
    }

    /**
     * Test of getRightFieldName method, of class NestedNodeConfigImpl.
     */
    @Test
    public void testGetRightFieldName() {
        System.out.println("getRightFieldName");
        NestedNodeConfigImpl instance = new NestedNodeConfigImpl();
        String expResult = null;
        String result = instance.getRightFieldName();
        assertEquals(expResult, result);
    }

    /**
     * Test of setRightFieldName method, of class NestedNodeConfigImpl.
     */
    @Test
    public void testSetRightFieldName() {
        System.out.println("setRightFieldName");
        String rightFieldName = null;
        NestedNodeConfigImpl instance = new NestedNodeConfigImpl();
        instance.setRightFieldName(rightFieldName);
    }

    /**
     * Test of getLeftFieldName method, of class NestedNodeConfigImpl.
     */
    @Test
    public void testGetLeftFieldName() {
        System.out.println("getLeftFieldName");
        NestedNodeConfigImpl instance = new NestedNodeConfigImpl();
        String expResult = null;
        String result = instance.getLeftFieldName();
        assertEquals(expResult, result);
    }

    /**
     * Test of setLeftFieldName method, of class NestedNodeConfigImpl.
     */
    @Test
    public void testSetLeftFieldName() {
        System.out.println("setLeftFieldName");
        String leftFieldName = null;
        NestedNodeConfigImpl instance = new NestedNodeConfigImpl();
        instance.setLeftFieldName(leftFieldName);
    }

    /**
     * Test of getLevelFieldName method, of class NestedNodeConfigImpl.
     */
    @Test
    public void testGetLevelFieldName() {
        System.out.println("getLevelFieldName");
        NestedNodeConfigImpl instance = new NestedNodeConfigImpl();
        String expResult = null;
        String result = instance.getLevelFieldName();
        assertEquals(expResult, result);
    }

    /**
     * Test of setLevelFieldName method, of class NestedNodeConfigImpl.
     */
    @Test
    public void testSetLevelFieldName() {
        System.out.println("setLevelFieldName");
        String levelFieldName = null;
        NestedNodeConfigImpl instance = new NestedNodeConfigImpl();
        instance.setLevelFieldName(levelFieldName);
    }

    /**
     * Test of getParentFieldName method, of class NestedNodeConfigImpl.
     */
    @Test
    public void testGetParentFieldName() {
        System.out.println("getParentFieldName");
        NestedNodeConfigImpl instance = new NestedNodeConfigImpl();
        String expResult = null;
        String result = instance.getParentFieldName();
        assertEquals(expResult, result);
    }

    /**
     * Test of setParentFieldName method, of class NestedNodeConfigImpl.
     */
    @Test
    public void testSetParentFieldName() {
        System.out.println("setParentFieldName");
        String parentFieldName = null;
        NestedNodeConfigImpl instance = new NestedNodeConfigImpl();
        instance.setParentFieldName(parentFieldName);
    }

    /**
     * Test of toString method, of class NestedNodeConfigImpl.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        NestedNodeConfigImpl instance = new NestedNodeConfigImpl();
        String expResult = "[leftFieldName: null, rightFieldName:null, levelFieldName: null, parentFieldName:null]";
        String result = instance.toString();
        assertEquals(expResult, result);
    }
    
}
