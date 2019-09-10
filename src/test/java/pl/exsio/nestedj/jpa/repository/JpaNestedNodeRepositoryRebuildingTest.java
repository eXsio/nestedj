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

import static org.junit.Assert.*;

@Transactional
public class JpaNestedNodeRepositoryRebuildingTest extends FunctionalJpaNestedjTest {

    @Test
    public void testInitializeTree() {
        try {

            this.removeTree();
            TestNode x = this.createTestNode("x");
            x.setTreeLeft(0L);
            x.setTreeRight(0L);
            x.setTreeLevel(0L);
            this.em.persist(x);
            this.em.flush();

            assertEquals(0L, (long) x.getTreeLeft());
            assertEquals(0L, (long) x.getTreeRight());

            this.jpaRepository.rebuildTree();
            em.refresh(x);
            printNode("x", x);
            assertEquals(1, (long) x.getTreeLeft());
            assertEquals(2, (long) x.getTreeRight());

        } catch (InvalidNodesHierarchyException ex) {
            fail("something went wrong:" + ex.getMessage());
        }
        assertSecondTreeIntact();
    }

    @Test
    public void testDestroyTree() {
        jpaRepository.destroyTree();
        em.flush();
        em.clear();

        TestNode a = this.findNode("a");
        TestNode e = this.findNode("e");
        TestNode b = this.findNode("b");
        TestNode d = this.findNode("d");
        TestNode g = this.findNode("g");
        TestNode c = this.findNode("c");
        TestNode h = this.findNode("h");
        TestNode f = this.findNode("f");

        assertEquals(0, (long) a.getTreeLeft());
        assertEquals(0, (long) a.getTreeRight());
        assertEquals(0, (long) b.getTreeLeft());
        assertEquals(0, (long) b.getTreeRight());
        assertEquals(0, (long) c.getTreeLeft());
        assertEquals(0, (long) c.getTreeRight());
        assertEquals(0, (long) d.getTreeLeft());
        assertEquals(0, (long) d.getTreeRight());
        assertEquals(0, (long) e.getTreeLeft());
        assertEquals(0, (long) e.getTreeRight());
        assertEquals(0, (long) f.getTreeLeft());
        assertEquals(0, (long) f.getTreeRight());
        assertEquals(0, (long) g.getTreeLeft());
        assertEquals(0, (long) g.getTreeRight());
        assertEquals(0, (long) h.getTreeLeft());
        assertEquals(0, (long) h.getTreeRight());

        assertNull(this.getParent(a));
        assertSame(this.getParent(b), a);
        assertSame(this.getParent(c), a);
        assertSame(this.getParent(d), b);
        assertSame(this.getParent(e), b);
        assertSame(this.getParent(f), c);
        assertSame(this.getParent(g), c);
        assertSame(this.getParent(h), g);

        assertEquals(0, (long) e.getTreeLevel());
        assertEquals(0, (long) f.getTreeLevel());
        assertEquals(0, (long) g.getTreeLevel());
        assertEquals(0, (long) b.getTreeLevel());
        assertEquals(0, (long) c.getTreeLevel());
        assertEquals(0, (long) h.getTreeLevel());
        assertEquals(0, (long) a.getTreeLevel());
        assertEquals(0, (long) d.getTreeLevel());

        assertSecondTreeIntact();

    }

    @Test
    public void testRebuildTree() {
        try {

            this.breakTree();
            this.jpaRepository.rebuildTree();

            em.flush();
            em.clear();

            TestNode a = this.findNode("a");
            TestNode e = this.findNode("e");
            TestNode b = this.findNode("b");
            TestNode d = this.findNode("d");
            TestNode g = this.findNode("g");
            TestNode c = this.findNode("c");
            TestNode h = this.findNode("h");
            TestNode f = this.findNode("f");

            assertNull(this.getParent(a));
            assertSame(this.getParent(b), a);
            assertNull(this.getParent(c));
            assertSame(this.getParent(d), b);
            assertSame(this.getParent(e), b);
            assertSame(this.getParent(f), c);
            assertSame(this.getParent(g), c);
            assertSame(this.getParent(h), g);

            assertEquals(2, (long) e.getTreeLevel());
            assertEquals(1, (long) f.getTreeLevel());
            assertEquals(1, (long) g.getTreeLevel());
            assertEquals(1, (long) b.getTreeLevel());
            assertEquals(0, (long) c.getTreeLevel());
            assertEquals(2, (long) h.getTreeLevel());

        } catch (InvalidNodesHierarchyException ex) {
            fail("something went wrong:" + ex.getMessage());
        }
        assertSecondTreeIntact();
    }

    @Test
    public void testRebuildWithSecondRoot() {

        TestNode i = this.createTestNode("i");
        TestNode j = this.createTestNode("j");
        TestNode k = this.createTestNode("k");
        TestNode a = this.findNode("a");
        this.jpaRepository.insertAsNextSiblingOf(i, a);
        this.jpaRepository.insertAsLastChildOf(j, i);
        this.jpaRepository.insertAsLastChildOf(k, i);

        this.em.createQuery("update TestNode set treeLeft = 0, treeRight = 0, treeLevel = 0 where discriminator = 'tree_1'").executeUpdate();
        em.flush();
        em.clear();
        this.jpaRepository.rebuildTree();

        a = this.findNode("a");
        TestNode b = this.findNode("b");
        TestNode c = this.findNode("c");
        TestNode d = this.findNode("d");
        TestNode e = this.findNode("e");
        TestNode g = this.findNode("g");
        TestNode f = this.findNode("f");
        TestNode h = this.findNode("h");

        i = em.find(TestNode.class, i.getId());
        j = em.find(TestNode.class, j.getId());
        k = em.find(TestNode.class, k.getId());

        printNode("i", i);
        printNode("j", j);
        printNode("k", k);

        assertNull(this.getParent(a));
        assertSame(this.getParent(b), a);
        assertSame(this.getParent(c), a);
        assertSame(this.getParent(d), b);
        assertSame(this.getParent(e), b);
        assertSame(this.getParent(f), c);
        assertSame(this.getParent(g), c);
        assertSame(this.getParent(h), g);
        assertNull(this.getParent(i));
        assertSame(this.getParent(j), i);
        assertSame(this.getParent(k), i);

        assertSecondTreeIntact();
    }

}
