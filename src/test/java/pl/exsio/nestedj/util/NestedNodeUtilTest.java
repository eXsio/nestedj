/* 
 * The MIT License
 *
 * Copyright 2015 exsio.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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
