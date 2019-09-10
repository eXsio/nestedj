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
package pl.exsio.nestedj.jpa.repository;

import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;
import pl.exsio.nestedj.ex.InvalidNodesHierarchyException;
import pl.exsio.nestedj.jpa.FunctionalJpaNestedjTest;
import pl.exsio.nestedj.model.TestNode;

import static org.junit.Assert.assertEquals;

@Transactional
public class JpaNestedNodeRepositoryInsertingTest extends FunctionalJpaNestedjTest {

    @Test(expected = InvalidNodesHierarchyException.class)
    public void testInsertParentToChildAsSibling() {
        TestNode a = this.findNode("a");
        TestNode e = this.findNode("e");
        this.jpaRepository.insertAsNextSiblingOf(a, e);
        assertSecondTreeIntact();
    }

    @Test(expected = InvalidNodesHierarchyException.class)
    public void testInsertParentToChildAsChild() {
        TestNode a = this.findNode("a");
        TestNode e = this.findNode("e");
        this.jpaRepository.insertAsLastChildOf(a, e);
        assertSecondTreeIntact();
    }

    @Test(expected = InvalidNodesHierarchyException.class)
    public void testInsertAsNextSiblingSameNode() {
        TestNode a = this.findNode("a");

        this.jpaRepository.insertAsNextSiblingOf(a, a);

        assertSecondTreeIntact();
    }

    @Test(expected = InvalidNodesHierarchyException.class)
    public void testInsertAsLastChildSameNode() {
        TestNode b = this.findNode("b");

        this.jpaRepository.insertAsLastChildOf(b, b);
        assertSecondTreeIntact();
    }

    @Test(expected = InvalidNodesHierarchyException.class)
    public void testInsertAsPrevSiblingSameNode() {
        TestNode c = this.findNode("c");

        this.jpaRepository.insertAsPrevSiblingOf(c, c);
        assertSecondTreeIntact();
    }

    @Test(expected = InvalidNodesHierarchyException.class)
    public void testInsertAsFirstChildSameNode() {
        TestNode d = this.findNode("d");

        this.jpaRepository.insertAsFirstChildOf(d, d);

        assertSecondTreeIntact();
    }

    @Test
    public void testInsertAsFirstChildOfInsert() {

        TestNode i = this.createTestNode("i");
        TestNode e = this.findNode("e");
        this.jpaRepository.insertAsFirstChildOf(i, e);
        TestNode a = this.findNode("a");
        TestNode b = this.findNode("b");
        TestNode h = this.findNode("h");

        assertEquals(6, (long) i.getTreeLeft());
        assertEquals(7, (long) i.getTreeRight());
        assertEquals(18, (long) a.getTreeRight());
        assertEquals(9, (long) b.getTreeRight());
        assertEquals(14, (long) h.getTreeLeft());
        assertEquals(15, (long) h.getTreeRight());
        assertEquals((long) i.getTreeLevel(), e.getTreeLevel() + 1);
        assertSecondTreeIntact();
    }

    @Test
    public void testInsertAsFirstChildOfInsertNextToSibling() {

        TestNode i = this.createTestNode("i");
        TestNode b = this.findNode("b");
        this.jpaRepository.insertAsFirstChildOf(i, b);
        TestNode a = this.findNode("a");
        em.flush();
        em.refresh(i);
        em.refresh(b);
        printNode("i", i);
        b = findNode("b");
        TestNode h = this.findNode("h");
        TestNode d = this.findNode("d");
        TestNode e = this.findNode("e");

        assertEquals(3, (long) i.getTreeLeft());
        assertEquals(4, (long) i.getTreeRight());
        assertEquals(18, (long) a.getTreeRight());
        assertEquals(2, (long) b.getTreeLeft());
        assertEquals(9, (long) b.getTreeRight());
        assertEquals(5, (long) d.getTreeLeft());
        assertEquals(6, (long) d.getTreeRight());
        assertEquals(7, (long) e.getTreeLeft());
        assertEquals(8, (long) e.getTreeRight());
        assertEquals(14, (long) h.getTreeLeft());
        assertEquals(15, (long) h.getTreeRight());
        assertEquals((long) i.getTreeLevel(), b.getTreeLevel() + 1);
        assertSecondTreeIntact();
    }

    @Test
    public void testInsertAsLastChildOfInsert() {

        TestNode j = this.createTestNode("j");
        TestNode b = this.findNode("b");
        this.jpaRepository.insertAsLastChildOf(j, b);
        TestNode a = this.findNode("a");
        TestNode h = this.findNode("h");
        TestNode c = this.findNode("c");

        assertEquals(7, (long) j.getTreeLeft());
        assertEquals(8, (long) j.getTreeRight());
        assertEquals(18, (long) a.getTreeRight());
        assertEquals(14, (long) h.getTreeLeft());
        assertEquals(15, (long) h.getTreeRight());
        assertEquals(10, (long) c.getTreeLeft());
        assertEquals((long) j.getTreeLevel(), b.getTreeLevel() + 1);
        assertSecondTreeIntact();
    }

    @Test
    public void testInsertAsPrevSiblingOfInsert() {

        TestNode k = this.createTestNode("k");
        TestNode e = this.findNode("e");
        this.jpaRepository.insertAsPrevSiblingOf(k, e);
        em.flush();
        em.clear();
        TestNode a = this.findNode("a");
        TestNode h = this.findNode("h");
        TestNode c = this.findNode("c");

        assertEquals(5, (long) k.getTreeLeft());
        assertEquals(6, (long) k.getTreeRight());
        assertEquals(18, (long) a.getTreeRight());
        assertEquals(14, (long) h.getTreeLeft());
        assertEquals(15, (long) h.getTreeRight());
        assertEquals(10, (long) c.getTreeLeft());
        assertEquals(k.getTreeLevel(), e.getTreeLevel());
        assertEquals(k.getParentId(), e.getParentId());
        assertSecondTreeIntact();
    }

    @Test
    public void testInsertAsNextSiblingOfInsert() {

        TestNode m = this.createTestNode("m");
        TestNode h = this.findNode("h");
        this.jpaRepository.insertAsNextSiblingOf(m, h);
        TestNode a = this.findNode("a");
        TestNode g = this.findNode("g");
        TestNode c = this.findNode("c");

        assertEquals(14, (long) m.getTreeLeft());
        assertEquals(15, (long) m.getTreeRight());
        assertEquals(18, (long) a.getTreeRight());
        assertEquals(16, (long) g.getTreeRight());
        assertEquals(17, (long) c.getTreeRight());
        assertEquals(m.getTreeLevel(), h.getTreeLevel());
        assertEquals(m.getParentId(), h.getParentId());
        assertSecondTreeIntact();
    }




}
