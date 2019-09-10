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
package pl.exsio.nestedj.repository;

import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;
import pl.exsio.nestedj.FunctionalNestedjTest;
import pl.exsio.nestedj.ex.InvalidNodesHierarchyException;
import pl.exsio.nestedj.model.TestNode;
import pl.exsio.nestedj.model.Tree;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

@Transactional
public class NestedNodeRepositoryTest extends FunctionalNestedjTest {

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

            this.nodeRepository.rebuildTree();
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
        nodeRepository.destroyTree();
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
            this.nodeRepository.rebuildTree();

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

    @Test(expected = InvalidNodesHierarchyException.class)
    public void testInsertParentToChildAsSibling() {
        TestNode a = this.findNode("a");
        TestNode e = this.findNode("e");
        this.nodeRepository.insertAsNextSiblingOf(a, e);
        assertSecondTreeIntact();
    }

    @Test(expected = InvalidNodesHierarchyException.class)
    public void testInsertParentToChildAsChild() {
        TestNode a = this.findNode("a");
        TestNode e = this.findNode("e");
        this.nodeRepository.insertAsLastChildOf(a, e);
        assertSecondTreeIntact();
    }

    @Test
    public void testGetParents() {
        TestNode h = this.findNode("h");
        List<TestNode> parents = (List<TestNode>) this.nodeRepository.getParents(h);
        assertEquals(3, parents.size());
        assertEquals("g", parents.get(0).getName());
        assertEquals("c", parents.get(1).getName());
        assertEquals("a", parents.get(2).getName());
        assertSecondTreeIntact();
    }

    @Test
    public void testGetPrevSibling() {
        TestNode c = this.findNode("c");
        Optional<TestNode> prevSibling = this.nodeRepository.getPrevSibling(c);
        assertTrue(prevSibling.isPresent());
        assertEquals("b", prevSibling.get().getName());
    }

    @Test
    public void testGetNextSibling() {
        TestNode b = this.findNode("b");
        Optional<TestNode> nextSibling = this.nodeRepository.getNextSibling(b);
        assertTrue(nextSibling.isPresent());
        assertEquals("c", nextSibling.get().getName());
    }

    @Test
    public void testGetPrevSiblingRoot() {

        TestNode y = this.createTestNode("y");
        try {
            y.setTreeLeft(0L);
            y.setTreeRight(0L);
            y.setTreeLevel(0L);
            this.em.persist(y);
            this.em.flush();
            this.nodeRepository.rebuildTree();
            em.refresh(y);
        } catch (InvalidNodesHierarchyException ex) {
            fail("something went wrong while creating a new root level node:" + ex.getMessage());
        }

        // ensure node y was built as a root level node
        assertEquals(0, (long) y.getTreeLevel());
        assertNull(y.getParentId());
        assertEquals(17, (long) y.getTreeLeft());
        assertEquals(18, (long) y.getTreeRight());

        Optional<TestNode> prevSiblingRoot = this.nodeRepository.getPrevSibling(y);
        assertTrue(prevSiblingRoot.isPresent());
        assertEquals("a", prevSiblingRoot.get().getName());
        assertSecondTreeIntact();
    }

    @Test
    public void testGetNextSiblingRoot() {

        TestNode z = this.createTestNode("z");
        try {
            z.setTreeLeft(0L);
            z.setTreeRight(0L);
            z.setTreeLevel(0L);
            this.em.persist(z);
            this.em.flush();
            this.nodeRepository.rebuildTree();
            em.refresh(z);
        } catch (InvalidNodesHierarchyException ex) {
            fail("something went wrong while creating a new root level node:" + ex.getMessage());
        }

        // ensure node z was built as a root level node
        assertEquals(0, (long) z.getTreeLevel());
        assertNull(z.getParentId());
        assertEquals(17, (long) z.getTreeLeft());
        assertEquals(18, (long) z.getTreeRight());

        TestNode a = this.findNode("a");

        Optional<TestNode> nextSiblingRoot = this.nodeRepository.getNextSibling(a);
        assertTrue(nextSiblingRoot.isPresent());
        assertEquals("z", nextSiblingRoot.get().getName());
        assertSecondTreeIntact();
    }

    @Test
    public void testGetTree() {
        Tree<Long, TestNode> tree = this.nodeRepository.getTree(this.findNode("a"));
        assertEquals("a", tree.getNode().getName());
        assertEquals("b", tree.getChildren().get(0).getNode().getName());
        assertEquals(2, tree.getChildren().size());
        assertEquals(2, tree.getChildren().get(0).getChildren().size());
        assertEquals(2, tree.getChildren().get(1).getChildren().size());
        assertEquals(1, tree.getChildren().get(1).getChildren().get(1).getChildren().size());
        assertTrue(tree.getChildren().get(1).getChildren().get(0).getChildren().isEmpty());
        assertTrue(tree.getChildren().get(0).getChildren().get(0).getChildren().isEmpty());
        assertSecondTreeIntact();
    }

    @Test
    public void testGetTreeAsList() {
        List<TestNode> list = (List<TestNode>) this.nodeRepository.getTreeAsList(this.findNode("a"));
        assertEquals(8, list.size());
        assertSecondTreeIntact();
    }

    @Test
    public void testGetParent() {
        TestNode b = this.findNode("b");
        Optional<TestNode> parent = this.nodeRepository.getParent(b);
        assertTrue(parent.isPresent());
        assertEquals("a", parent.get().getName());
        assertSecondTreeIntact();
    }

    @Test
    public void testRemoveSubtreeWithoutChildren() {

        TestNode d = this.findNode("d");
        this.nodeRepository.removeSubtree(d);
        TestNode a = this.findNode("a");
        TestNode e = this.findNode("e");
        TestNode b = this.findNode("b");
        TestNode g = this.findNode("g");
        TestNode c = this.findNode("c");
        TestNode h = this.findNode("h");
        TestNode f = this.findNode("f");

        assertEquals(3, (long) e.getTreeLeft());
        assertEquals(4, (long) e.getTreeRight());
        assertEquals(5, (long) b.getTreeRight());
        assertEquals(10, (long) h.getTreeLeft());
        assertEquals(11, (long) h.getTreeRight());
        assertEquals(14, (long) a.getTreeRight());
        assertEquals(6, (long) c.getTreeLeft());
        assertEquals(13, (long) c.getTreeRight());
        assertEquals(9, (long) g.getTreeLeft());
        assertEquals(12, (long) g.getTreeRight());
        assertSecondTreeIntact();

    }

    @Test
    public void testRemoveSubtree() {

        TestNode b = this.findNode("b");
        this.nodeRepository.removeSubtree(b);
        TestNode a = this.findNode("a");
        TestNode g = this.findNode("g");
        TestNode c = this.findNode("c");
        TestNode h = this.findNode("h");
        TestNode f = this.findNode("f");

        assertEquals(6, (long) h.getTreeLeft());
        assertEquals(7, (long) h.getTreeRight());
        assertEquals(10, (long) a.getTreeRight());
        assertEquals(2, (long) c.getTreeLeft());
        assertEquals(9, (long) c.getTreeRight());
        assertEquals(5, (long) g.getTreeLeft());
        assertEquals(8, (long) g.getTreeRight());
        assertSecondTreeIntact();

    }

    @Test
    public void testRemoveSingleNodeThatHasChildren() {

        TestNode b = this.findNode("b");
        this.nodeRepository.removeSingle(b);
        TestNode a = this.findNode("a");
        TestNode e = this.findNode("e");
        TestNode d = this.findNode("d");
        TestNode g = this.findNode("g");
        TestNode c = this.findNode("c");
        TestNode h = this.findNode("h");

        assertEquals(2, (long) d.getTreeLeft());
        assertEquals(3, (long) d.getTreeRight());
        assertEquals(4, (long) e.getTreeLeft());
        assertEquals(5, (long) e.getTreeRight());
        assertEquals(10, (long) h.getTreeLeft());
        assertEquals(11, (long) h.getTreeRight());
        assertEquals(14, (long) a.getTreeRight());
        assertEquals(6, (long) c.getTreeLeft());
        assertEquals(13, (long) c.getTreeRight());
        assertEquals(9, (long) g.getTreeLeft());
        assertEquals(12, (long) g.getTreeRight());
        assertEquals(1, (long) d.getTreeLevel());
        assertEquals(1, (long) e.getTreeLevel());
        assertSecondTreeIntact();
    }

    @Test
    public void testRemoveSingleNode() {

        TestNode d = this.findNode("d");
        this.nodeRepository.removeSingle(d);
        TestNode a = this.findNode("a");
        TestNode g = this.findNode("g");
        TestNode c = this.findNode("c");
        TestNode e = this.findNode("e");

        assertEquals(3, (long) e.getTreeLeft());
        assertEquals(4, (long) e.getTreeRight());
        assertEquals(14, (long) a.getTreeRight());
        assertEquals(6, (long) c.getTreeLeft());
        assertEquals(13, (long) c.getTreeRight());
        assertEquals(9, (long) g.getTreeLeft());
        assertEquals(12, (long) g.getTreeRight());
        assertSecondTreeIntact();

    }

    @Test(expected = InvalidNodesHierarchyException.class)
    public void testInsertAsNextSiblingSameNode() {
        TestNode a = this.findNode("a");

        this.nodeRepository.insertAsNextSiblingOf(a, a);

        assertSecondTreeIntact();
    }

    @Test(expected = InvalidNodesHierarchyException.class)
    public void testInsertAsLastChildSameNode() {
        TestNode b = this.findNode("b");

        this.nodeRepository.insertAsLastChildOf(b, b);
        assertSecondTreeIntact();
    }

    @Test(expected = InvalidNodesHierarchyException.class)
    public void testInsertAsPrevSiblingSameNode() {
        TestNode c = this.findNode("c");

        this.nodeRepository.insertAsPrevSiblingOf(c, c);
        assertSecondTreeIntact();
    }

    @Test(expected = InvalidNodesHierarchyException.class)
    public void testInsertAsFirstChildSameNode() {
        TestNode d = this.findNode("d");

        this.nodeRepository.insertAsFirstChildOf(d, d);

        assertSecondTreeIntact();
    }

    @Test
    public void testInsertAsLastChildOfDeepMove() {
        TestNode b = this.findNode("b");
        TestNode a = this.findNode("a");
        this.nodeRepository.insertAsLastChildOf(b, a);
        TestNode d = this.findNode("d");
        TestNode g = this.findNode("g");
        TestNode c = this.findNode("c");

        em.refresh(b);
        em.refresh(a);
        assertEquals(2, (long) c.getTreeLeft());
        assertEquals(9, (long) c.getTreeRight());
        assertEquals(10, (long) b.getTreeLeft());
        assertEquals(15, (long) b.getTreeRight());
        assertEquals(5, (long) g.getTreeLeft());
        assertEquals(8, (long) g.getTreeRight());
        assertEquals(11, (long) d.getTreeLeft());
        assertEquals(12, (long) d.getTreeRight());
        assertEquals(1, (long) b.getTreeLevel());
        assertEquals(2, (long) d.getTreeLevel());
        assertSame(this.getParent(b), a);
        assertSecondTreeIntact();
    }

    @Test
    public void testInsertAsFirstChildOfDeepMove() {
        TestNode c = this.findNode("c");
        TestNode a = this.findNode("a");
        this.nodeRepository.insertAsFirstChildOf(c, a);
        TestNode d = this.findNode("d");
        TestNode g = this.findNode("g");
        TestNode b = this.findNode("b");

        em.refresh(c);
        em.refresh(a);
        assertEquals(2, (long) c.getTreeLeft());
        assertEquals(9, (long) c.getTreeRight());
        assertEquals(10, (long) b.getTreeLeft());
        assertEquals(15, (long) b.getTreeRight());
        assertEquals(5, (long) g.getTreeLeft());
        assertEquals(8, (long) g.getTreeRight());
        assertEquals(11, (long) d.getTreeLeft());
        assertEquals(12, (long) d.getTreeRight());
        assertEquals(2, (long) g.getTreeLevel());
        assertEquals(1, (long) c.getTreeLevel());
        assertSame(this.getParent(c), a);
        assertSecondTreeIntact();
    }

    @Test
    public void testInsertAsNextSiblingOfDeepMove() {
        TestNode b = this.findNode("b");
        TestNode a = this.findNode("a");
        this.nodeRepository.insertAsNextSiblingOf(b, a);
        TestNode d = this.findNode("d");
        TestNode g = this.findNode("g");
        TestNode e = this.findNode("e");

        em.refresh(b);
        em.refresh(a);
        assertEquals(11, (long) b.getTreeLeft());
        assertEquals(16, (long) b.getTreeRight());
        assertEquals(1, (long) a.getTreeLeft());
        assertEquals(10, (long) a.getTreeRight());
        assertEquals(5, (long) g.getTreeLeft());
        assertEquals(8, (long) g.getTreeRight());
        assertEquals(12, (long) d.getTreeLeft());
        assertEquals(13, (long) d.getTreeRight());
        assertEquals(0, (long) b.getTreeLevel());
        assertEquals(1, (long) d.getTreeLevel());
        assertEquals(1, (long) e.getTreeLevel());
        assertNull(this.getParent(b));
        assertSecondTreeIntact();
    }

    @Test
    public void testInsertAsPrevSiblingOfDeepMove() {
        TestNode c = this.findNode("c");
        TestNode a = this.findNode("a");
        this.nodeRepository.insertAsPrevSiblingOf(c, a);
        TestNode d = this.findNode("d");
        TestNode g = this.findNode("g");
        TestNode f = this.findNode("f");
        TestNode h = this.findNode("h");

        em.refresh(c);
        em.refresh(a);
        assertEquals(1, (long) c.getTreeLeft());
        assertEquals(8, (long) c.getTreeRight());
        assertEquals(9, (long) a.getTreeLeft());
        assertEquals(16, (long) a.getTreeRight());
        assertEquals(4, (long) g.getTreeLeft());
        assertEquals(7, (long) g.getTreeRight());
        assertEquals(11, (long) d.getTreeLeft());
        assertEquals(12, (long) d.getTreeRight());
        assertEquals(0, (long) c.getTreeLevel());
        assertEquals(1, (long) f.getTreeLevel());
        assertEquals(1, (long) g.getTreeLevel());
        assertEquals(2, (long) h.getTreeLevel());
        assertNull(this.getParent(c));
        assertSecondTreeIntact();
    }

    @Test
    public void testInsertAsPrevSiblingOfMoveRight() {
        TestNode d = this.findNode("d");
        TestNode g = this.findNode("g");
        this.nodeRepository.insertAsPrevSiblingOf(d, g);
        TestNode f = this.findNode("f");
        TestNode c = this.findNode("c");
        TestNode a = this.findNode("a");
        TestNode b = this.findNode("b");
        TestNode e = this.findNode("e");
        TestNode h = this.findNode("h");

        em.refresh(d);
        em.refresh(g);
        assertEquals(3, (long) e.getTreeLeft());
        assertEquals(4, (long) e.getTreeRight());
        assertEquals(5, (long) b.getTreeRight());
        assertEquals(7, (long) f.getTreeLeft());
        assertEquals(8, (long) f.getTreeRight());
        assertEquals(11, (long) g.getTreeLeft());
        assertEquals(14, (long) g.getTreeRight());
        assertEquals(9, (long) d.getTreeLeft());
        assertEquals(10, (long) d.getTreeRight());
        assertEquals(12, (long) h.getTreeLeft());
        assertEquals(13, (long) h.getTreeRight());
        assertEquals(15, (long) c.getTreeRight());
        assertEquals(16, (long) a.getTreeRight());
        assertEquals(2, (long) d.getTreeLevel());

        assertSame(this.getParent(d), c);
        assertSecondTreeIntact();
    }

    @Test
    public void testInsertAsPrevSiblingOfMoveLeft() {
        TestNode g = this.findNode("g");
        TestNode e = this.findNode("e");
        this.nodeRepository.insertAsPrevSiblingOf(g, e);
        TestNode c = this.findNode("c");
        TestNode a = this.findNode("a");
        TestNode b = this.findNode("b");
        TestNode h = this.findNode("h");

        em.refresh(g);
        em.refresh(e);
        assertEquals(5, (long) g.getTreeLeft());
        assertEquals(8, (long) g.getTreeRight());
        assertEquals(6, (long) h.getTreeLeft());
        assertEquals(7, (long) h.getTreeRight());
        assertEquals(9, (long) e.getTreeLeft());
        assertEquals(10, (long) e.getTreeRight());
        assertEquals(11, (long) b.getTreeRight());
        assertEquals(12, (long) c.getTreeLeft());
        assertEquals(15, (long) c.getTreeRight());
        assertEquals(16, (long) a.getTreeRight());
        assertEquals(2, (long) g.getTreeLevel());
        assertEquals(3, (long) h.getTreeLevel());
        assertSame(this.getParent(g), b);
        assertSecondTreeIntact();
    }

    @Test
    public void testInsertAsNextSiblingOfMoveRight() {
        TestNode d = this.findNode("d");
        TestNode f = this.findNode("f");
        this.nodeRepository.insertAsNextSiblingOf(d, f);
        TestNode g = this.findNode("g");
        TestNode c = this.findNode("c");
        TestNode a = this.findNode("a");
        TestNode b = this.findNode("b");
        TestNode e = this.findNode("e");
        TestNode h = this.findNode("h");

        em.refresh(d);
        em.refresh(f);
        assertEquals(3, (long) e.getTreeLeft());
        assertEquals(4, (long) e.getTreeRight());
        assertEquals(5, (long) b.getTreeRight());
        assertEquals(7, (long) f.getTreeLeft());
        assertEquals(8, (long) f.getTreeRight());
        assertEquals(11, (long) g.getTreeLeft());
        assertEquals(14, (long) g.getTreeRight());
        assertEquals(9, (long) d.getTreeLeft());
        assertEquals(10, (long) d.getTreeRight());
        assertEquals(12, (long) h.getTreeLeft());
        assertEquals(13, (long) h.getTreeRight());
        assertEquals(15, (long) c.getTreeRight());
        assertEquals(16, (long) a.getTreeRight());
        assertEquals(2, (long) d.getTreeLevel());
        assertSame(this.getParent(d), c);
        assertSecondTreeIntact();
    }

    @Test
    public void testInsertAsNextSiblingOfMoveLeft() {
        TestNode g = this.findNode("g");
        TestNode d = this.findNode("d");
        this.nodeRepository.insertAsNextSiblingOf(g, d);
        TestNode c = this.findNode("c");
        TestNode a = this.findNode("a");
        TestNode b = this.findNode("b");
        TestNode e = this.findNode("e");
        TestNode h = this.findNode("h");

        em.refresh(d);
        em.refresh(g);
        assertEquals(5, (long) g.getTreeLeft());
        assertEquals(8, (long) g.getTreeRight());
        assertEquals(6, (long) h.getTreeLeft());
        assertEquals(7, (long) h.getTreeRight());
        assertEquals(9, (long) e.getTreeLeft());
        assertEquals(10, (long) e.getTreeRight());
        assertEquals(11, (long) b.getTreeRight());
        assertEquals(12, (long) c.getTreeLeft());
        assertEquals(15, (long) c.getTreeRight());
        assertEquals(16, (long) a.getTreeRight());
        assertEquals(2, (long) g.getTreeLevel());
        assertEquals(3, (long) h.getTreeLevel());
        assertSame(this.getParent(g), b);
        assertSecondTreeIntact();
    }

    @Test
    public void testInsertAsLastChildOfMoveLeft() {
        TestNode g = this.findNode("g");
        TestNode b = this.findNode("b");
        this.nodeRepository.insertAsLastChildOf(g, b);
        TestNode f = this.findNode("f");
        TestNode c = this.findNode("c");
        TestNode a = this.findNode("a");
        TestNode d = this.findNode("d");
        TestNode h = this.findNode("h");

        em.refresh(g);
        em.refresh(b);
        assertEquals(7, (long) g.getTreeLeft());
        assertEquals(10, (long) g.getTreeRight());
        assertEquals(8, (long) h.getTreeLeft());
        assertEquals(9, (long) h.getTreeRight());
        assertEquals(11, (long) b.getTreeRight());
        assertEquals(13, (long) f.getTreeLeft());
        assertEquals(14, (long) f.getTreeRight());
        assertEquals(12, (long) c.getTreeLeft());
        assertEquals(15, (long) c.getTreeRight());
        assertEquals(16, (long) a.getTreeRight());
        assertEquals(2, (long) g.getTreeLevel());
        assertEquals(3, (long) h.getTreeLevel());
        assertSame(this.getParent(g), b);
        assertSecondTreeIntact();
    }

    @Test
    public void testInsertAsLastChildOfMoveRight() {
        TestNode d = this.findNode("d");
        TestNode g = this.findNode("g");
        this.nodeRepository.insertAsLastChildOf(d, g);
        TestNode f = this.findNode("f");
        TestNode c = this.findNode("c");
        TestNode a = this.findNode("a");
        TestNode b = this.findNode("b");
        TestNode e = this.findNode("e");
        TestNode h = this.findNode("h");

        em.refresh(d);
        em.refresh(g);
        assertEquals(3, (long) e.getTreeLeft());
        assertEquals(4, (long) e.getTreeRight());
        assertEquals(5, (long) b.getTreeRight());
        assertEquals(7, (long) f.getTreeLeft());
        assertEquals(9, (long) g.getTreeLeft());
        assertEquals(12, (long) d.getTreeLeft());
        assertEquals(13, (long) d.getTreeRight());
        assertEquals(10, (long) h.getTreeLeft());
        assertEquals(11, (long) h.getTreeRight());
        assertEquals(15, (long) c.getTreeRight());
        assertEquals(16, (long) a.getTreeRight());
        assertEquals(3, (long) d.getTreeLevel());
        assertSame(this.getParent(d), g);
        assertSecondTreeIntact();
    }

    @Test
    public void testInsertAsFirstChildOfMoveRight() {
        TestNode d = findNode("d");
        TestNode g = findNode("g");
        this.nodeRepository.insertAsFirstChildOf(d, g);

        em.refresh(d);
        em.refresh(g);
        TestNode f = findNode("f");
        TestNode c = findNode("c");
        TestNode a = findNode("a");
        TestNode b = findNode("b");
        TestNode e = findNode("e");
        TestNode h = findNode("h");

        assertEquals(3, (long) e.getTreeLeft());
        assertEquals(4, (long) e.getTreeRight());
        assertEquals(5, (long) b.getTreeRight());
        assertEquals(7, (long) f.getTreeLeft());
        assertEquals(9, (long) g.getTreeLeft());
        assertEquals(10, (long) d.getTreeLeft());
        assertEquals(11, (long) d.getTreeRight());
        assertEquals(12, (long) h.getTreeLeft());
        assertEquals(13, (long) h.getTreeRight());
        assertEquals(15, (long) c.getTreeRight());
        assertEquals(16, (long) a.getTreeRight());
        assertEquals(3, (long) d.getTreeLevel());
        assertSame(this.getParent(d), g);
        assertSecondTreeIntact();
    }

    @Test
    public void testInsertAsFirstChildOfMoveLeft() {
        TestNode g = this.findNode("g");
        TestNode b = this.findNode("b");
        this.nodeRepository.insertAsFirstChildOf(g, b);
        TestNode f = this.findNode("f");
        TestNode c = this.findNode("c");
        TestNode a = this.findNode("a");
        TestNode h = this.findNode("h");

        em.refresh(g);
        em.refresh(b);
        assertEquals(3, (long) g.getTreeLeft());
        assertEquals(6, (long) g.getTreeRight());
        assertEquals(13, (long) f.getTreeLeft());
        assertEquals(14, (long) f.getTreeRight());
        assertEquals(12, (long) c.getTreeLeft());
        assertEquals(15, (long) c.getTreeRight());
        assertEquals(16, (long) a.getTreeRight());
        assertEquals(2, (long) g.getTreeLevel());
        assertEquals(3, (long) h.getTreeLevel());
        assertSame(this.getParent(g), b);
        assertSecondTreeIntact();
    }

    @Test
    public void testGetChildren() {

        List result = (List) this.nodeRepository.getChildren(this.findNode("a"));
        assertEquals(2, result.size());
        assertSecondTreeIntact();
    }

    @Test
    public void testInsertAsFirstChildOfInsert() {

        TestNode i = this.createTestNode("i");
        TestNode e = this.findNode("e");
        this.nodeRepository.insertAsFirstChildOf(i, e);
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
        this.nodeRepository.insertAsFirstChildOf(i, b);
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
        this.nodeRepository.insertAsLastChildOf(j, b);
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
        this.nodeRepository.insertAsPrevSiblingOf(k, e);
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
        this.nodeRepository.insertAsNextSiblingOf(m, h);
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

    @Test
    public void testInsertAsNextSiblingOfMoveEdge() {
        TestNode h = this.findNode("h");
        TestNode c = this.findNode("c");
        this.nodeRepository.insertAsLastChildOf(h, c);

        TestNode a = this.findNode("a");
        TestNode b = this.findNode("b");
        c = this.findNode("c");
        TestNode d = this.findNode("d");
        TestNode e = this.findNode("e");
        TestNode g = this.findNode("g");
        TestNode f = this.findNode("f");
        h = this.findNode("h");

        assertEquals(1, (long) a.getTreeLeft());
        assertEquals(16, (long) a.getTreeRight());
        assertEquals(2, (long) b.getTreeLeft());
        assertEquals(7, (long) b.getTreeRight());
        assertEquals(8, (long) c.getTreeLeft());
        assertEquals(15, (long) c.getTreeRight());
        assertEquals(3, (long) d.getTreeLeft());
        assertEquals(4, (long) d.getTreeRight());
        assertEquals(5, (long) e.getTreeLeft());
        assertEquals(6, (long) e.getTreeRight());
        assertEquals(9, (long) f.getTreeLeft());
        assertEquals(10, (long) f.getTreeRight());
        assertEquals(11, (long) g.getTreeLeft());
        assertEquals(12, (long) g.getTreeRight());
        assertEquals(13, (long) h.getTreeLeft());
        assertEquals(14, (long) h.getTreeRight());
        assertSame(this.getParent(h), c);
        assertSecondTreeIntact();
    }

    @Test
    public void testInsertAsNextSiblingOfMoveSecondRoot() {

        TestNode c = this.findNode("c");
        TestNode a = this.findNode("a");
        this.nodeRepository.insertAsNextSiblingOf(c, a);

        a = this.findNode("a");
        TestNode b = this.findNode("b");
        c = this.findNode("c");
        TestNode d = this.findNode("d");
        TestNode e = this.findNode("e");
        TestNode g = this.findNode("g");
        TestNode f = this.findNode("f");
        TestNode h = this.findNode("h");

        assertEquals(1, (long) a.getTreeLeft());
        assertEquals(8, (long) a.getTreeRight());
        assertEquals(2, (long) b.getTreeLeft());
        assertEquals(7, (long) b.getTreeRight());
        assertEquals(9, (long) c.getTreeLeft());
        assertEquals(16, (long) c.getTreeRight());
        assertEquals(3, (long) d.getTreeLeft());
        assertEquals(4, (long) d.getTreeRight());
        assertEquals(5, (long) e.getTreeLeft());
        assertEquals(6, (long) e.getTreeRight());
        assertEquals(10, (long) f.getTreeLeft());
        assertEquals(11, (long) f.getTreeRight());
        assertEquals(12, (long) g.getTreeLeft());
        assertEquals(15, (long) g.getTreeRight());
        assertEquals(13, (long) h.getTreeLeft());
        assertEquals(14, (long) h.getTreeRight());
        assertNull(this.getParent(c));
        assertSecondTreeIntact();
    }

    @Test
    public void testMultipleOperations() {

        TestNode i = this.createTestNode("i");
        TestNode j = this.createTestNode("j");
        TestNode k = this.createTestNode("k");
        TestNode l = this.createTestNode("l");
        TestNode m = this.createTestNode("m");
        TestNode a = this.findNode("a");

        this.nodeRepository.insertAsNextSiblingOf(i, a);
        em.flush();
        em.clear();

        i = em.find(TestNode.class, i.getId());
        printNode("i", i);
        this.nodeRepository.insertAsLastChildOf(j, i);
        em.flush();
        em.clear();

        i = em.find(TestNode.class, i.getId());
        j = em.find(TestNode.class, j.getId());
        printNode("i", i);
        printNode("j", j);
        this.nodeRepository.insertAsFirstChildOf(k, i);
        em.flush();
        em.clear();

        j = em.find(TestNode.class, j.getId());
        k = em.find(TestNode.class, k.getId());
        this.nodeRepository.insertAsNextSiblingOf(l, k);
        em.flush();
        em.clear();
        i = em.find(TestNode.class, i.getId());
        j = em.find(TestNode.class, j.getId());
        k = em.find(TestNode.class, k.getId());
        l = em.find(TestNode.class, l.getId());

        this.nodeRepository.insertAsPrevSiblingOf(m, l);
        em.flush();
        em.clear();

        System.out.println("ASSERTS");
        a = this.findNode("a");
        TestNode b = this.findNode("b");
        TestNode c = this.findNode("c");
        TestNode d = this.findNode("d");
        TestNode e = this.findNode("e");
        TestNode g = this.findNode("g");
        TestNode f = this.findNode("f");
        TestNode h = this.findNode("h");
        i = this.findNode("i");
        j = this.findNode("j");
        k = this.findNode("k");
        l = this.findNode("l");
        m = this.findNode("m");

        assertEquals(1, (long) a.getTreeLeft());
        assertEquals(16, (long) a.getTreeRight());
        assertEquals(2, (long) b.getTreeLeft());
        assertEquals(7, (long) b.getTreeRight());
        assertEquals(8, (long) c.getTreeLeft());
        assertEquals(15, (long) c.getTreeRight());
        assertEquals(3, (long) d.getTreeLeft());
        assertEquals(4, (long) d.getTreeRight());
        assertEquals(5, (long) e.getTreeLeft());
        assertEquals(6, (long) e.getTreeRight());
        assertEquals(9, (long) f.getTreeLeft());
        assertEquals(10, (long) f.getTreeRight());
        assertEquals(11, (long) g.getTreeLeft());
        assertEquals(14, (long) g.getTreeRight());
        assertEquals(12, (long) h.getTreeLeft());
        assertEquals(13, (long) h.getTreeRight());

        assertEquals(17, (long) i.getTreeLeft());
        assertEquals(26, (long) i.getTreeRight());
        assertEquals(24, (long) j.getTreeLeft());
        assertEquals(25, (long) j.getTreeRight());
        assertEquals(18, (long) k.getTreeLeft());
        assertEquals(19, (long) k.getTreeRight());
        assertEquals(22, (long) l.getTreeLeft());
        assertEquals(23, (long) l.getTreeRight());
        assertEquals(20, (long) m.getTreeLeft());
        assertEquals(21, (long) m.getTreeRight());

        assertNull(this.getParent(i));
        assertSame(this.getParent(j), i);
        assertSame(this.getParent(k), i);
        assertSecondTreeIntact();

        this.em.createQuery("update TestNode set treeLeft = 0, treeRight = 0, treeLevel = 0 where discriminator = 'tree_1'").executeUpdate();
        em.flush();
        em.clear();

        nodeRepository.rebuildTree();
        em.flush();
        em.clear();

        assertSecondTreeIntact();

        System.out.println("ASSERTS AFTER REBUILD");

        a = this.findNode("a");
        b = this.findNode("b");
        c = this.findNode("c");
        d = this.findNode("d");
        e = this.findNode("e");
        g = this.findNode("g");
        f = this.findNode("f");
        h = this.findNode("h");
        i = this.findNode("i");
        j = this.findNode("j");
        k = this.findNode("k");
        l = this.findNode("l");
        m = this.findNode("m");

        assertEquals(1, (long) a.getTreeLeft());
        assertEquals(16, (long) a.getTreeRight());
        assertEquals(2, (long) b.getTreeLeft());
        assertEquals(7, (long) b.getTreeRight());
        assertEquals(8, (long) c.getTreeLeft());
        assertEquals(15, (long) c.getTreeRight());
        assertEquals(3, (long) d.getTreeLeft());
        assertEquals(4, (long) d.getTreeRight());
        assertEquals(5, (long) e.getTreeLeft());
        assertEquals(6, (long) e.getTreeRight());
        assertEquals(9, (long) f.getTreeLeft());
        assertEquals(10, (long) f.getTreeRight());
        assertEquals(11, (long) g.getTreeLeft());
        assertEquals(14, (long) g.getTreeRight());
        assertEquals(12, (long) h.getTreeLeft());
        assertEquals(13, (long) h.getTreeRight());

        assertEquals(17, (long) i.getTreeLeft());
        assertEquals(26, (long) i.getTreeRight());
        assertEquals(18, (long) j.getTreeLeft());
        assertEquals(19, (long) j.getTreeRight());
        assertEquals(20, (long) k.getTreeLeft());
        assertEquals(21, (long) k.getTreeRight());
        assertEquals(22, (long) l.getTreeLeft());
        assertEquals(23, (long) l.getTreeRight());
        assertEquals(24, (long) m.getTreeLeft());
        assertEquals(25, (long) m.getTreeRight());

        nodeRepository.removeSingle(i);

        em.flush();
        em.clear();

        a = this.findNode("a");
        b = this.findNode("b");
        c = this.findNode("c");
        d = this.findNode("d");
        e = this.findNode("e");
        g = this.findNode("g");
        f = this.findNode("f");
        h = this.findNode("h");
        j = this.findNode("j");
        k = this.findNode("k");
        l = this.findNode("l");
        m = this.findNode("m");

        System.out.println("ASSERTS AFTER REMOVE SINGLE");

        assertEquals(1, (long) a.getTreeLeft());
        assertEquals(16, (long) a.getTreeRight());
        assertEquals(2, (long) b.getTreeLeft());
        assertEquals(7, (long) b.getTreeRight());
        assertEquals(8, (long) c.getTreeLeft());
        assertEquals(15, (long) c.getTreeRight());
        assertEquals(3, (long) d.getTreeLeft());
        assertEquals(4, (long) d.getTreeRight());
        assertEquals(5, (long) e.getTreeLeft());
        assertEquals(6, (long) e.getTreeRight());
        assertEquals(9, (long) f.getTreeLeft());
        assertEquals(10, (long) f.getTreeRight());
        assertEquals(11, (long) g.getTreeLeft());
        assertEquals(14, (long) g.getTreeRight());
        assertEquals(12, (long) h.getTreeLeft());
        assertEquals(13, (long) h.getTreeRight());

        assertEquals(17, (long) j.getTreeLeft());
        assertEquals(18, (long) j.getTreeRight());
        assertEquals(0, (long) j.getTreeLevel());
        assertNull(j.getParentId());
        assertEquals(19, (long) k.getTreeLeft());
        assertEquals(20, (long) k.getTreeRight());
        assertEquals(0, (long) k.getTreeLevel());
        assertNull(k.getParentId());
        assertEquals(21, (long) l.getTreeLeft());
        assertEquals(22, (long) l.getTreeRight());
        assertEquals(0, (long) l.getTreeLevel());
        assertNull(l.getParentId());
        assertEquals(23, (long) m.getTreeLeft());
        assertEquals(24, (long) m.getTreeRight());
        assertEquals(0, (long) m.getTreeLevel());
        assertNull(m.getParentId());

        nodeRepository.removeSubtree(a);
        em.flush();
        em.clear();

        j = this.findNode("j");
        k = this.findNode("k");
        l = this.findNode("l");
        m = this.findNode("m");

        System.out.println("ASSERTS AFTER REMOVE SUBTREE");

        assertEquals(1, (long) j.getTreeLeft());
        assertEquals(2, (long) j.getTreeRight());
        assertEquals(0, (long) j.getTreeLevel());
        assertNull(j.getParentId());
        assertEquals(3, (long) k.getTreeLeft());
        assertEquals(4, (long) k.getTreeRight());
        assertEquals(0, (long) k.getTreeLevel());
        assertNull(k.getParentId());
        assertEquals(5, (long) l.getTreeLeft());
        assertEquals(6, (long) l.getTreeRight());
        assertEquals(0, (long) l.getTreeLevel());
        assertNull(l.getParentId());
        assertEquals(7, (long) m.getTreeLeft());
        assertEquals(8, (long) m.getTreeRight());
        assertEquals(0, (long) m.getTreeLevel());
        assertNull(m.getParentId());
    }

    @Test
    public void testRebuildWithSecondRoot() {

        TestNode i = this.createTestNode("i");
        TestNode j = this.createTestNode("j");
        TestNode k = this.createTestNode("k");
        TestNode a = this.findNode("a");
        this.nodeRepository.insertAsNextSiblingOf(i, a);
        this.nodeRepository.insertAsLastChildOf(j, i);
        this.nodeRepository.insertAsLastChildOf(k, i);

        this.em.createQuery("update TestNode set treeLeft = 0, treeRight = 0, treeLevel = 0 where discriminator = 'tree_1'").executeUpdate();
        em.flush();
        em.clear();
        this.nodeRepository.rebuildTree();

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

    private void assertSecondTreeIntact() {
        TestNode a2 = this.findNode("a2");
        TestNode b2 = this.findNode("b2");
        TestNode c2 = this.findNode("c2");
        TestNode d2 = this.findNode("d2");
        TestNode e2 = this.findNode("e2");
        TestNode g2 = this.findNode("g2");
        TestNode f2 = this.findNode("f2");
        TestNode h2 = this.findNode("h2");

        assertEquals(1, (long) a2.getTreeLeft());
        assertEquals(16, (long) a2.getTreeRight());
        assertEquals(2, (long) b2.getTreeLeft());
        assertEquals(7, (long) b2.getTreeRight());
        assertEquals(8, (long) c2.getTreeLeft());
        assertEquals(15, (long) c2.getTreeRight());
        assertEquals(3, (long) d2.getTreeLeft());
        assertEquals(4, (long) d2.getTreeRight());
        assertEquals(5, (long) e2.getTreeLeft());
        assertEquals(6, (long) e2.getTreeRight());
        assertEquals(9, (long) f2.getTreeLeft());
        assertEquals(10, (long) f2.getTreeRight());
        assertEquals(11, (long) g2.getTreeLeft());
        assertEquals(14, (long) g2.getTreeRight());
        assertEquals(12, (long) h2.getTreeLeft());
        assertEquals(13, (long) h2.getTreeRight());

        assertNull(this.getParent(a2));
        assertSame(this.getParent(b2), a2);
        assertSame(this.getParent(c2), a2);
        assertSame(this.getParent(d2), b2);
        assertSame(this.getParent(e2), b2);
        assertSame(this.getParent(f2), c2);
        assertSame(this.getParent(g2), c2);
        assertSame(this.getParent(h2), g2);

    }

    private void breakTree() {

        this.em.createQuery("update TestNode set parentId = null where name='c' and discriminator = 'tree_1'").executeUpdate();
        this.em.createQuery("update TestNode set treeLeft = 0, treeRight = 0, treeLevel = 0 where discriminator = 'tree_1'").executeUpdate();

    }

    private void removeTree() {
        this.em.createQuery("delete from TestNode where discriminator = 'tree_1'").executeUpdate();
    }

}
