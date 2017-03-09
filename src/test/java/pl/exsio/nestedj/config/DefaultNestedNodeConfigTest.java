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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DefaultNestedNodeConfigTest {
    
    public DefaultNestedNodeConfigTest() {
    }

    @Test
    public void testGetRightFieldName() {
        NestedNodeConfigImpl instance = new NestedNodeConfigImpl();
        String expResult = null;
        String result = instance.getRightFieldName();
        assertEquals(expResult, result);
    }

    @Test
    public void testSetRightFieldName() {
        String rightFieldName = "rightField";
        NestedNodeConfigImpl instance = new NestedNodeConfigImpl();
        instance.setRightFieldName(rightFieldName);
        assertEquals(instance.getRightFieldName(), rightFieldName);
    }

    @Test
    public void testSetIdFieldName() {
        String idFieldName = "idField";
        NestedNodeConfigImpl instance = new NestedNodeConfigImpl();
        instance.setIdFieldName(idFieldName);
        assertEquals(instance.getIdFieldName(), idFieldName);
    }

    @Test
    public void testGetIdFieldName() {
        NestedNodeConfigImpl instance = new NestedNodeConfigImpl();
        String expResult = null;
        String result = instance.getIdFieldName();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetLeftFieldName() {
        NestedNodeConfigImpl instance = new NestedNodeConfigImpl();
        String expResult = null;
        String result = instance.getLeftFieldName();
        assertEquals(expResult, result);
    }

    @Test
    public void testSetLeftFieldName() {
        String leftFieldName = "leftField";
        NestedNodeConfigImpl instance = new NestedNodeConfigImpl();
        instance.setLeftFieldName(leftFieldName);
        assertEquals(instance.getLeftFieldName(), leftFieldName);
    }

    @Test
    public void testGetLevelFieldName() {
        NestedNodeConfigImpl instance = new NestedNodeConfigImpl();
        String expResult = null;
        String result = instance.getLevelFieldName();
        assertEquals(expResult, result);
    }

    @Test
    public void testSetLevelFieldName() {
        String levelFieldName = "levelField";
        NestedNodeConfigImpl instance = new NestedNodeConfigImpl();
        instance.setLevelFieldName(levelFieldName);
        assertEquals(instance.getLevelFieldName(), levelFieldName);
    }

    @Test
    public void testGetParentFieldName() {
        NestedNodeConfigImpl instance = new NestedNodeConfigImpl();
        String expResult = null;
        String result = instance.getParentFieldName();
        assertEquals(expResult, result);
    }

    @Test
    public void testSetParentFieldName() {
        String parentFieldName = "parentFIeld";
        NestedNodeConfigImpl instance = new NestedNodeConfigImpl();
        instance.setParentFieldName(parentFieldName);
        assertEquals(instance.getParentFieldName(), parentFieldName);
    }

}