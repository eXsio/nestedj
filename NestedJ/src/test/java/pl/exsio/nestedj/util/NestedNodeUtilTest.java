package pl.exsio.nestedj.util;

import static org.junit.Assert.*;
import org.junit.Test;
import pl.exsio.nestedj.FunctionalNestedjTest;
import pl.exsio.nestedj.config.NestedNodeConfig;
import pl.exsio.nestedj.model.InheritedTestNodeImpl;
import pl.exsio.nestedj.model.TestNodeImpl;

/**
 *
 * @author exsio
 */
public class NestedNodeUtilTest extends FunctionalNestedjTest {

    public NestedNodeUtilTest() {
    }

    @Test
    public void testIsNodeNew() {

        TestNodeImpl node = new TestNodeImpl();
        boolean expResult = true;
        boolean result = NestedNodeUtil.isNodeNew(node);
        assertEquals(expResult, result);
    }

    @Test
    public void testGetNodeConfigWithoutInheritence() {

        NestedNodeConfig config = NestedNodeUtil.getNodeConfig(TestNodeImpl.class);
        assertEquals(config.getEntityName(), "TestNodeImpl");
        assertEquals(config.getLeftFieldName(), "lft");
        assertEquals(config.getRightFieldName(), "rgt");
        assertEquals(config.getLevelFieldName(), "lvl");
        assertEquals(config.getParentFieldName(), "parent");
        assertEquals(config.getIdFieldName(), "id");

    }

    @Test
    public void testGetNodeConfigWithInheritence() {

        NestedNodeConfig config = NestedNodeUtil.getNodeConfig(InheritedTestNodeImpl.class);
        assertEquals(config.getEntityName(), "InheritedTestNodeImpl");
        assertEquals(config.getRightFieldName(), "inherited_rgt");
        assertEquals(config.getLevelFieldName(), "inherited_lvl");
        assertEquals(config.getLeftFieldName(), "lft");
        assertEquals(config.getParentFieldName(), "parent");
        assertEquals(config.getIdFieldName(), "id");

    }

}
