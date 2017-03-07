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
     * Test of getRightFieldName method, of class NestedNodeConfigImpl.
     */
    @Test
    public void testGetRightFieldName() {
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
        String rightFieldName = "rightField";
        NestedNodeConfigImpl instance = new NestedNodeConfigImpl();
        instance.setRightFieldName(rightFieldName);
        assertEquals(instance.getRightFieldName(), rightFieldName);
    }
    
    /**
     * Test of setIdFieldName method, of class NestedNodeConfigImpl.
     */
    @Test
    public void testSetIdFieldName() {
        String idFieldName = "idField";
        NestedNodeConfigImpl instance = new NestedNodeConfigImpl();
        instance.setIdFieldName(idFieldName);
        assertEquals(instance.getIdFieldName(), idFieldName);
    }
    
    /**
     * Test of getIdFieldName method, of class NestedNodeConfigImpl.
     */
    @Test
    public void testGetIdFieldName() {
        NestedNodeConfigImpl instance = new NestedNodeConfigImpl();
        String expResult = null;
        String result = instance.getIdFieldName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getLeftFieldName method, of class NestedNodeConfigImpl.
     */
    @Test
    public void testGetLeftFieldName() {
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
        String leftFieldName = "leftField";
        NestedNodeConfigImpl instance = new NestedNodeConfigImpl();
        instance.setLeftFieldName(leftFieldName);
        assertEquals(instance.getLeftFieldName(), leftFieldName);
    }

    /**
     * Test of getLevelFieldName method, of class NestedNodeConfigImpl.
     */
    @Test
    public void testGetLevelFieldName() {
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
        String levelFieldName = "levelField";
        NestedNodeConfigImpl instance = new NestedNodeConfigImpl();
        instance.setLevelFieldName(levelFieldName);
        assertEquals(instance.getLevelFieldName(), levelFieldName);
    }

    /**
     * Test of getParentFieldName method, of class NestedNodeConfigImpl.
     */
    @Test
    public void testGetParentFieldName() {
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
        String parentFieldName = "parentFIeld";
        NestedNodeConfigImpl instance = new NestedNodeConfigImpl();
        instance.setParentFieldName(parentFieldName);
        assertEquals(instance.getParentFieldName(), parentFieldName);
    }

    /**
     * Test of toString method, of class NestedNodeConfigImpl.
     */
    @Test
    public void testToString() {
        NestedNodeConfigImpl instance = new NestedNodeConfigImpl();
        String expResult = "[leftFieldName: null, rightFieldName:null, levelFieldName: null, parentFieldName:null]";
        String result = instance.toString();
        assertEquals(expResult, result);
    }
    
}
