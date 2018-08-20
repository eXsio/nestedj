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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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

            assertTrue(x.getTreeLeft() == 0L);
            assertTrue(x.getTreeRight() == 0L);

            this.nodeRepository.rebuildTree();
            em.refresh(x);
            printNode("x", x);
            assertTrue(x.getTreeLeft() == 1);
            assertTrue(x.getTreeRight() == 2);

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

        assertTrue(a.getTreeLeft() == 0);
        assertTrue(a.getTreeRight() == 0);
        assertTrue(b.getTreeLeft() == 0);
        assertTrue(b.getTreeRight() == 0);
        assertTrue(c.getTreeLeft() == 0);
        assertTrue(c.getTreeRight() == 0);
        assertTrue(d.getTreeLeft() == 0);
        assertTrue(d.getTreeRight() == 0);
        assertTrue(e.getTreeLeft() == 0);
        assertTrue(e.getTreeRight() == 0);
        assertTrue(f.getTreeLeft() == 0);
        assertTrue(f.getTreeRight() == 0);
        assertTrue(g.getTreeLeft() == 0);
        assertTrue(g.getTreeRight() == 0);
        assertTrue(h.getTreeLeft() == 0);
        assertTrue(h.getTreeRight() == 0);

        assertTrue(this.getParent(a) == null);
        assertTrue(this.getParent(b) == a);
        assertTrue(this.getParent(c) == a);
        assertTrue(this.getParent(d) == b);
        assertTrue(this.getParent(e) == b);
        assertTrue(this.getParent(f) == c);
        assertTrue(this.getParent(g) == c);
        assertTrue(this.getParent(h) == g);

        assertTrue(e.getTreeLevel() == 0);
        assertTrue(f.getTreeLevel() == 0);
        assertTrue(g.getTreeLevel() == 0);
        assertTrue(b.getTreeLevel() == 0);
        assertTrue(c.getTreeLevel() == 0);
        assertTrue(h.getTreeLevel() == 0);
        assertTrue(a.getTreeLevel() == 0);
        assertTrue(d.getTreeLevel() == 0);

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

            assertTrue(this.getParent(a) == null);
            assertTrue(this.getParent(b) == a);
            assertTrue(this.getParent(c) == null);
            assertTrue(this.getParent(d) == b);
            assertTrue(this.getParent(e) == b);
            assertTrue(this.getParent(f) == c);
            assertTrue(this.getParent(g) == c);
            assertTrue(this.getParent(h) == g);

            assertTrue(e.getTreeLevel() == 2);
            assertTrue(f.getTreeLevel() == 1);
            assertTrue(g.getTreeLevel() == 1);
            assertTrue(b.getTreeLevel() == 1);
            assertTrue(c.getTreeLevel() == 0);
            assertTrue(h.getTreeLevel() == 2);

        } catch (InvalidNodesHierarchyException ex) {
            fail("something went wrong:" + ex.getMessage());
        }
        assertSecondTreeIntact();
    }

    @Test
    public void testInsertParentToChildAsSibling() {
        TestNode a = this.findNode("a");
        TestNode e = this.findNode("e");
        try {
            this.nodeRepository.insertAsNextSiblingOf(a, e);
            fail("this action should have triggered an exception");
        } catch (InvalidNodesHierarchyException ex) {
        }
        assertSecondTreeIntact();
    }

    @Test
    public void testInsertParentToChildAsChild() {
        TestNode a = this.findNode("a");
        TestNode e = this.findNode("e");
        try {
            this.nodeRepository.insertAsLastChildOf(a, e);
            fail("this action should have triggered an exception");
        } catch (InvalidNodesHierarchyException ex) {
        }
        assertSecondTreeIntact();
    }

    @Test
    public void testGetParents() {
        TestNode h = this.findNode("h");
        List<TestNode> parents = (List<TestNode>) this.nodeRepository.getParents(h);
        assertTrue(parents.size() == 3);
        assertTrue(parents.get(0).getName().equals("g"));
        assertTrue(parents.get(1).getName().equals("c"));
        assertTrue(parents.get(2).getName().equals("a"));
        assertSecondTreeIntact();
    }

    @Test
    public void testGetTree() {
        Tree<Long, TestNode> tree = this.nodeRepository.getTree(this.findNode("a"));
        assertTrue(tree.getNode().getName().equals("a"));
        assertTrue(tree.getChildren().get(0).getNode().getName().equals("b"));
        assertTrue(tree.getChildren().size() == 2);
        assertTrue(tree.getChildren().get(0).getChildren().size() == 2);
        assertTrue(tree.getChildren().get(1).getChildren().size() == 2);
        assertTrue(tree.getChildren().get(1).getChildren().get(1).getChildren().size() == 1);
        assertTrue(tree.getChildren().get(1).getChildren().get(0).getChildren().isEmpty());
        assertTrue(tree.getChildren().get(0).getChildren().get(0).getChildren().isEmpty());
        assertSecondTreeIntact();
    }

    @Test
    public void testGetTreeAsList() {
        List<TestNode> list = (List<TestNode>) this.nodeRepository.getTreeAsList(this.findNode("a"));
        assertTrue(list.size() == 8);
        assertSecondTreeIntact();
    }

    @Test
    public void testGetParent() {
        TestNode b = this.findNode("b");
        Optional<TestNode> parent = this.nodeRepository.getParent(b);
        assertTrue(parent.isPresent());
        assertTrue(parent.get() instanceof TestNode);
        assertTrue(parent.get().getName().equals("a"));
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

        assertTrue(e.getTreeLeft() == 3);
        assertTrue(e.getTreeRight() == 4);
        assertTrue(b.getTreeRight() == 5);
        assertTrue(h.getTreeLeft() == 10);
        assertTrue(h.getTreeRight() == 11);
        assertTrue(a.getTreeRight() == 14);
        assertTrue(c.getTreeLeft() == 6);
        assertTrue(c.getTreeRight() == 13);
        assertTrue(g.getTreeLeft() == 9);
        assertTrue(g.getTreeRight() == 12);
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

        assertTrue(h.getTreeLeft() == 6);
        assertTrue(h.getTreeRight() == 7);
        assertTrue(a.getTreeRight() == 10);
        assertTrue(c.getTreeLeft() == 2);
        assertTrue(c.getTreeRight() == 9);
        assertTrue(g.getTreeLeft() == 5);
        assertTrue(g.getTreeRight() == 8);
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
        TestNode f = this.findNode("f");

        assertTrue(d.getTreeLeft() == 2);
        assertTrue(d.getTreeRight() == 3);
        assertTrue(e.getTreeLeft() == 4);
        assertTrue(e.getTreeRight() == 5);
        assertTrue(h.getTreeLeft() == 10);
        assertTrue(h.getTreeRight() == 11);
        assertTrue(a.getTreeRight() == 14);
        assertTrue(c.getTreeLeft() == 6);
        assertTrue(c.getTreeRight() == 13);
        assertTrue(g.getTreeLeft() == 9);
        assertTrue(g.getTreeRight() == 12);
        assertTrue(d.getTreeLevel() == 1);
        assertTrue(e.getTreeLevel() == 1);
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

        assertTrue(e.getTreeLeft() == 3);
        assertTrue(e.getTreeRight() == 4);
        assertTrue(a.getTreeRight() == 14);
        assertTrue(c.getTreeLeft() == 6);
        assertTrue(c.getTreeRight() == 13);
        assertTrue(g.getTreeLeft() == 9);
        assertTrue(g.getTreeRight() == 12);
        assertSecondTreeIntact();

    }

    @Test
    public void testInsertAsNextSiblingSameNode() {
        TestNode a = this.findNode("a");
        try {
            this.nodeRepository.insertAsNextSiblingOf(a, a);
            fail("this action should have triggered an exception");
        } catch (InvalidNodesHierarchyException ex) {
        }
        assertSecondTreeIntact();
    }

    @Test
    public void testInsertAsLastChildSameNode() {
        TestNode b = this.findNode("b");
        try {
            this.nodeRepository.insertAsLastChildOf(b, b);
            fail("this action should have triggered an exception");
        } catch (InvalidNodesHierarchyException ex) {
        }
        assertSecondTreeIntact();
    }

    @Test
    public void testInsertAsPrevSiblingSameNode() {
        TestNode c = this.findNode("c");
        try {
            this.nodeRepository.insertAsPrevSiblingOf(c, c);
            fail("this action should have triggered an exception");
        } catch (InvalidNodesHierarchyException ex) {
        }
        assertSecondTreeIntact();
    }

    @Test
    public void testInsertAsFirstChildSameNode() {
        TestNode d = this.findNode("d");
        try {
            this.nodeRepository.insertAsFirstChildOf(d, d);
            fail("this action should have triggered an exception");
        } catch (InvalidNodesHierarchyException ex) {
        }
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
        assertTrue(c.getTreeLeft() == 2);
        assertTrue(c.getTreeRight() == 9);
        assertTrue(b.getTreeLeft() == 10);
        assertTrue(b.getTreeRight() == 15);
        assertTrue(g.getTreeLeft() == 5);
        assertTrue(g.getTreeRight() == 8);
        assertTrue(d.getTreeLeft() == 11);
        assertTrue(d.getTreeRight() == 12);
        assertTrue(b.getTreeLevel() == 1);
        assertTrue(d.getTreeLevel() == 2);
        assertTrue(this.getParent(b) == a);
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
        assertTrue(c.getTreeLeft() == 2);
        assertTrue(c.getTreeRight() == 9);
        assertTrue(b.getTreeLeft() == 10);
        assertTrue(b.getTreeRight() == 15);
        assertTrue(g.getTreeLeft() == 5);
        assertTrue(g.getTreeRight() == 8);
        assertTrue(d.getTreeLeft() == 11);
        assertTrue(d.getTreeRight() == 12);
        assertTrue(g.getTreeLevel() == 2);
        assertTrue(c.getTreeLevel() == 1);
        assertTrue(this.getParent(c) == a);
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
        assertTrue(b.getTreeLeft() == 11);
        assertTrue(b.getTreeRight() == 16);
        assertTrue(a.getTreeLeft() == 1);
        assertTrue(a.getTreeRight() == 10);
        assertTrue(g.getTreeLeft() == 5);
        assertTrue(g.getTreeRight() == 8);
        assertTrue(d.getTreeLeft() == 12);
        assertTrue(d.getTreeRight() == 13);
        assertTrue(b.getTreeLevel() == 0);
        assertTrue(d.getTreeLevel() == 1);
        assertTrue(e.getTreeLevel() == 1);
        assertTrue(this.getParent(b) == null);
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
        assertTrue(c.getTreeLeft() == 1);
        assertTrue(c.getTreeRight() == 8);
        assertTrue(a.getTreeLeft() == 9);
        assertTrue(a.getTreeRight() == 16);
        assertTrue(g.getTreeLeft() == 4);
        assertTrue(g.getTreeRight() == 7);
        assertTrue(d.getTreeLeft() == 11);
        assertTrue(d.getTreeRight() == 12);
        assertTrue(c.getTreeLevel() == 0);
        assertTrue(f.getTreeLevel() == 1);
        assertTrue(g.getTreeLevel() == 1);
        assertTrue(h.getTreeLevel() == 2);
        assertTrue(this.getParent(c) == null);
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
        assertTrue(e.getTreeLeft() == 3);
        assertTrue(e.getTreeRight() == 4);
        assertTrue(b.getTreeRight() == 5);
        assertTrue(f.getTreeLeft() == 7);
        assertTrue(f.getTreeRight() == 8);
        assertTrue(g.getTreeLeft() == 11);
        assertTrue(g.getTreeRight() == 14);
        assertTrue(d.getTreeLeft() == 9);
        assertTrue(d.getTreeRight() == 10);
        assertTrue(h.getTreeLeft() == 12);
        assertTrue(h.getTreeRight() == 13);
        assertTrue(c.getTreeRight() == 15);
        assertTrue(a.getTreeRight() == 16);
        assertTrue(d.getTreeLevel() == 2);

        assertTrue(this.getParent(d) == c);
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
        assertTrue(g.getTreeLeft() == 5);
        assertTrue(g.getTreeRight() == 8);
        assertTrue(h.getTreeLeft() == 6);
        assertTrue(h.getTreeRight() == 7);
        assertTrue(e.getTreeLeft() == 9);
        assertTrue(e.getTreeRight() == 10);
        assertTrue(b.getTreeRight() == 11);
        assertTrue(c.getTreeLeft() == 12);
        assertTrue(c.getTreeRight() == 15);
        assertTrue(a.getTreeRight() == 16);
        assertTrue(g.getTreeLevel() == 2);
        assertTrue(h.getTreeLevel() == 3);
        assertTrue(this.getParent(g) == b);
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
        assertTrue(e.getTreeLeft() == 3);
        assertTrue(e.getTreeRight() == 4);
        assertTrue(b.getTreeRight() == 5);
        assertTrue(f.getTreeLeft() == 7);
        assertTrue(f.getTreeRight() == 8);
        assertTrue(g.getTreeLeft() == 11);
        assertTrue(g.getTreeRight() == 14);
        assertTrue(d.getTreeLeft() == 9);
        assertTrue(d.getTreeRight() == 10);
        assertTrue(h.getTreeLeft() == 12);
        assertTrue(h.getTreeRight() == 13);
        assertTrue(c.getTreeRight() == 15);
        assertTrue(a.getTreeRight() == 16);
        assertTrue(d.getTreeLevel() == 2);
        assertTrue(this.getParent(d) == c);
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
        assertTrue(g.getTreeLeft() == 5);
        assertTrue(g.getTreeRight() == 8);
        assertTrue(h.getTreeLeft() == 6);
        assertTrue(h.getTreeRight() == 7);
        assertTrue(e.getTreeLeft() == 9);
        assertTrue(e.getTreeRight() == 10);
        assertTrue(b.getTreeRight() == 11);
        assertTrue(c.getTreeLeft() == 12);
        assertTrue(c.getTreeRight() == 15);
        assertTrue(a.getTreeRight() == 16);
        assertTrue(g.getTreeLevel() == 2);
        assertTrue(h.getTreeLevel() == 3);
        assertTrue(this.getParent(g) == b);
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
        assertTrue(g.getTreeLeft() == 7);
        assertTrue(g.getTreeRight() == 10);
        assertTrue(h.getTreeLeft() == 8);
        assertTrue(h.getTreeRight() == 9);
        assertTrue(b.getTreeRight() == 11);
        assertTrue(f.getTreeLeft() == 13);
        assertTrue(f.getTreeRight() == 14);
        assertTrue(c.getTreeLeft() == 12);
        assertTrue(c.getTreeRight() == 15);
        assertTrue(a.getTreeRight() == 16);
        assertTrue(g.getTreeLevel() == 2);
        assertTrue(h.getTreeLevel() == 3);
        assertTrue(this.getParent(g) == b);
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
        assertTrue(e.getTreeLeft() == 3);
        assertTrue(e.getTreeRight() == 4);
        assertTrue(b.getTreeRight() == 5);
        assertTrue(f.getTreeLeft() == 7);
        assertTrue(g.getTreeLeft() == 9);
        assertTrue(d.getTreeLeft() == 12);
        assertTrue(d.getTreeRight() == 13);
        assertTrue(h.getTreeLeft() == 10);
        assertTrue(h.getTreeRight() == 11);
        assertTrue(c.getTreeRight() == 15);
        assertTrue(a.getTreeRight() == 16);
        assertTrue(d.getTreeLevel() == 3);
        assertTrue(this.getParent(d) == g);
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

        assertTrue(e.getTreeLeft() == 3);
        assertTrue(e.getTreeRight() == 4);
        assertTrue(b.getTreeRight() == 5);
        assertTrue(f.getTreeLeft() == 7);
        assertTrue(g.getTreeLeft() == 9);
        assertTrue(d.getTreeLeft() == 10);
        assertTrue(d.getTreeRight() == 11);
        assertTrue(h.getTreeLeft() == 12);
        assertTrue(h.getTreeRight() == 13);
        assertTrue(c.getTreeRight() == 15);
        assertTrue(a.getTreeRight() == 16);
        assertTrue(d.getTreeLevel() == 3);
        assertTrue(this.getParent(d) == g);
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
        assertTrue(g.getTreeLeft() == 3);
        assertTrue(g.getTreeRight() == 6);
        assertTrue(f.getTreeLeft() == 13);
        assertTrue(f.getTreeRight() == 14);
        assertTrue(c.getTreeLeft() == 12);
        assertTrue(c.getTreeRight() == 15);
        assertTrue(a.getTreeRight() == 16);
        assertTrue(g.getTreeLevel() == 2);
        assertTrue(h.getTreeLevel() == 3);
        assertTrue(this.getParent(g) == b);
        assertSecondTreeIntact();
    }

    @Test
    public void testGetChildren() {

        List result = (List) this.nodeRepository.getChildren(this.findNode("a"));
        assertTrue(result.size() == 2);
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

        assertTrue(i.getTreeLeft() == 6);
        assertTrue(i.getTreeRight() == 7);
        assertTrue(a.getTreeRight() == 18);
        assertTrue(b.getTreeRight() == 9);
        assertTrue(h.getTreeLeft() == 14);
        assertTrue(h.getTreeRight() == 15);
        assertTrue(i.getTreeLevel().equals(e.getTreeLevel() + 1));
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

        assertTrue(i.getTreeLeft() == 3);
        assertTrue(i.getTreeRight() == 4);
        assertTrue(a.getTreeRight() == 18);
        assertTrue(b.getTreeLeft() == 2);
        assertTrue(b.getTreeRight() == 9);
        assertTrue(d.getTreeLeft() == 5);
        assertTrue(d.getTreeRight() == 6);
        assertTrue(e.getTreeLeft() == 7);
        assertTrue(e.getTreeRight() == 8);
        assertTrue(h.getTreeLeft() == 14);
        assertTrue(h.getTreeRight() == 15);
        assertTrue(i.getTreeLevel().equals(b.getTreeLevel() + 1));
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

        assertTrue(j.getTreeLeft() == 7);
        assertTrue(j.getTreeRight() == 8);
        assertTrue(a.getTreeRight() == 18);
        assertTrue(h.getTreeLeft() == 14);
        assertTrue(h.getTreeRight() == 15);
        assertTrue(c.getTreeLeft() == 10);
        assertTrue(j.getTreeLevel().equals(b.getTreeLevel() + 1));
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

        assertTrue(k.getTreeLeft() == 5);
        assertTrue(k.getTreeRight() == 6);
        assertTrue(a.getTreeRight() == 18);
        assertTrue(h.getTreeLeft() == 14);
        assertTrue(h.getTreeRight() == 15);
        assertTrue(c.getTreeLeft() == 10);
        assertTrue(k.getTreeLevel().equals(e.getTreeLevel()));
        assertTrue(k.getParentId().equals(e.getParentId()));
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

        assertTrue(m.getTreeLeft() == 14);
        assertTrue(m.getTreeRight() == 15);
        assertTrue(a.getTreeRight() == 18);
        assertTrue(g.getTreeRight() == 16);
        assertTrue(c.getTreeRight() == 17);
        assertTrue(m.getTreeLevel().equals(h.getTreeLevel()));
        assertTrue(m.getParentId().equals(h.getParentId()));
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

        assertTrue(a.getTreeLeft() == 1);
        assertTrue(a.getTreeRight() == 16);
        assertTrue(b.getTreeLeft() == 2);
        assertTrue(b.getTreeRight() == 7);
        assertTrue(c.getTreeLeft() == 8);
        assertTrue(c.getTreeRight() == 15);
        assertTrue(d.getTreeLeft() == 3);
        assertTrue(d.getTreeRight() == 4);
        assertTrue(e.getTreeLeft() == 5);
        assertTrue(e.getTreeRight() == 6);
        assertTrue(f.getTreeLeft() == 9);
        assertTrue(f.getTreeRight() == 10);
        assertTrue(g.getTreeLeft() == 11);
        assertTrue(g.getTreeRight() == 12);
        assertTrue(h.getTreeLeft() == 13);
        assertTrue(h.getTreeRight() == 14);
        assertTrue(this.getParent(h) == c);
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

        assertTrue(a.getTreeLeft() == 1);
        assertTrue(a.getTreeRight() == 8);
        assertTrue(b.getTreeLeft() == 2);
        assertTrue(b.getTreeRight() == 7);
        assertTrue(c.getTreeLeft() == 9);
        assertTrue(c.getTreeRight() == 16);
        assertTrue(d.getTreeLeft() == 3);
        assertTrue(d.getTreeRight() == 4);
        assertTrue(e.getTreeLeft() == 5);
        assertTrue(e.getTreeRight() == 6);
        assertTrue(f.getTreeLeft() == 10);
        assertTrue(f.getTreeRight() == 11);
        assertTrue(g.getTreeLeft() == 12);
        assertTrue(g.getTreeRight() == 15);
        assertTrue(h.getTreeLeft() == 13);
        assertTrue(h.getTreeRight() == 14);
        assertTrue(this.getParent(c) == null);
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

        assertTrue(a.getTreeLeft() == 1);
        assertTrue(a.getTreeRight() == 16);
        assertTrue(b.getTreeLeft() == 2);
        assertTrue(b.getTreeRight() == 7);
        assertTrue(c.getTreeLeft() == 8);
        assertTrue(c.getTreeRight() == 15);
        assertTrue(d.getTreeLeft() == 3);
        assertTrue(d.getTreeRight() == 4);
        assertTrue(e.getTreeLeft() == 5);
        assertTrue(e.getTreeRight() == 6);
        assertTrue(f.getTreeLeft() == 9);
        assertTrue(f.getTreeRight() == 10);
        assertTrue(g.getTreeLeft() == 11);
        assertTrue(g.getTreeRight() == 14);
        assertTrue(h.getTreeLeft() == 12);
        assertTrue(h.getTreeRight() == 13);

        assertTrue(i.getTreeLeft() == 17);
        assertTrue(i.getTreeRight() == 26);
        assertTrue(j.getTreeLeft() == 24);
        assertTrue(j.getTreeRight() == 25);
        assertTrue(k.getTreeLeft() == 18);
        assertTrue(k.getTreeRight() == 19);
        assertTrue(l.getTreeLeft() == 22);
        assertTrue(l.getTreeRight() == 23);
        assertTrue(m.getTreeLeft() == 20);
        assertTrue(m.getTreeRight() == 21);

        assertTrue(this.getParent(i) == null);
        assertTrue(this.getParent(j) == i);
        assertTrue(this.getParent(k) == i);
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

        assertTrue(a.getTreeLeft() == 1);
        assertTrue(a.getTreeRight() == 16);
        assertTrue(b.getTreeLeft() == 2);
        assertTrue(b.getTreeRight() == 7);
        assertTrue(c.getTreeLeft() == 8);
        assertTrue(c.getTreeRight() == 15);
        assertTrue(d.getTreeLeft() == 3);
        assertTrue(d.getTreeRight() == 4);
        assertTrue(e.getTreeLeft() == 5);
        assertTrue(e.getTreeRight() == 6);
        assertTrue(f.getTreeLeft() == 9);
        assertTrue(f.getTreeRight() == 10);
        assertTrue(g.getTreeLeft() == 11);
        assertTrue(g.getTreeRight() == 14);
        assertTrue(h.getTreeLeft() == 12);
        assertTrue(h.getTreeRight() == 13);

        assertTrue(i.getTreeLeft() == 17);
        assertTrue(i.getTreeRight() == 26);
        assertTrue(j.getTreeLeft() == 18);
        assertTrue(j.getTreeRight() == 19);
        assertTrue(k.getTreeLeft() == 20);
        assertTrue(k.getTreeRight() == 21);
        assertTrue(l.getTreeLeft() == 22);
        assertTrue(l.getTreeRight() == 23);
        assertTrue(m.getTreeLeft() == 24);
        assertTrue(m.getTreeRight() == 25);

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

        assertTrue(a.getTreeLeft() == 1);
        assertTrue(a.getTreeRight() == 16);
        assertTrue(b.getTreeLeft() == 2);
        assertTrue(b.getTreeRight() == 7);
        assertTrue(c.getTreeLeft() == 8);
        assertTrue(c.getTreeRight() == 15);
        assertTrue(d.getTreeLeft() == 3);
        assertTrue(d.getTreeRight() == 4);
        assertTrue(e.getTreeLeft() == 5);
        assertTrue(e.getTreeRight() == 6);
        assertTrue(f.getTreeLeft() == 9);
        assertTrue(f.getTreeRight() == 10);
        assertTrue(g.getTreeLeft() == 11);
        assertTrue(g.getTreeRight() == 14);
        assertTrue(h.getTreeLeft() == 12);
        assertTrue(h.getTreeRight() == 13);

        assertTrue(j.getTreeLeft() == 17);
        assertTrue(j.getTreeRight() == 18);
        assertTrue(j.getTreeLevel() == 0);
        assertTrue(j.getParentId() == null);
        assertTrue(k.getTreeLeft() == 19);
        assertTrue(k.getTreeRight() == 20);
        assertTrue(k.getTreeLevel() == 0);
        assertTrue(k.getParentId() == null);
        assertTrue(l.getTreeLeft() == 21);
        assertTrue(l.getTreeRight() == 22);
        assertTrue(l.getTreeLevel() == 0);
        assertTrue(l.getParentId() == null);
        assertTrue(m.getTreeLeft() == 23);
        assertTrue(m.getTreeRight() == 24);
        assertTrue(m.getTreeLevel() == 0);
        assertTrue(m.getParentId() == null);

        nodeRepository.removeSubtree(a);
        em.flush();
        em.clear();

        j = this.findNode("j");
        k = this.findNode("k");
        l = this.findNode("l");
        m = this.findNode("m");

        System.out.println("ASSERTS AFTER REMOVE SUBTREE");

        assertTrue(j.getTreeLeft() == 1);
        assertTrue(j.getTreeRight() == 2);
        assertTrue(j.getTreeLevel() == 0);
        assertTrue(j.getParentId() == null);
        assertTrue(k.getTreeLeft() == 3);
        assertTrue(k.getTreeRight() == 4);
        assertTrue(k.getTreeLevel() == 0);
        assertTrue(k.getParentId() == null);
        assertTrue(l.getTreeLeft() == 5);
        assertTrue(l.getTreeRight() == 6);
        assertTrue(l.getTreeLevel() == 0);
        assertTrue(l.getParentId() == null);
        assertTrue(m.getTreeLeft() == 7);
        assertTrue(m.getTreeRight() == 8);
        assertTrue(m.getTreeLevel() == 0);
        assertTrue(m.getParentId() == null);
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

        assertTrue(this.getParent(a) == null);
        assertTrue(this.getParent(b) == a);
        assertTrue(this.getParent(c) == a);
        assertTrue(this.getParent(d) == b);
        assertTrue(this.getParent(e) == b);
        assertTrue(this.getParent(f) == c);
        assertTrue(this.getParent(g) == c);
        assertTrue(this.getParent(h) == g);
        assertTrue(this.getParent(i) == null);
        assertTrue(this.getParent(j) == i);
        assertTrue(this.getParent(k) == i);

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

        assertTrue(a2.getTreeLeft() == 1);
        assertTrue(a2.getTreeRight() == 16);
        assertTrue(b2.getTreeLeft() == 2);
        assertTrue(b2.getTreeRight() == 7);
        assertTrue(c2.getTreeLeft() == 8);
        assertTrue(c2.getTreeRight() == 15);
        assertTrue(d2.getTreeLeft() == 3);
        assertTrue(d2.getTreeRight() == 4);
        assertTrue(e2.getTreeLeft() == 5);
        assertTrue(e2.getTreeRight() == 6);
        assertTrue(f2.getTreeLeft() == 9);
        assertTrue(f2.getTreeRight() == 10);
        assertTrue(g2.getTreeLeft() == 11);
        assertTrue(g2.getTreeRight() == 14);
        assertTrue(h2.getTreeLeft() == 12);
        assertTrue(h2.getTreeRight() == 13);

        assertTrue(this.getParent(a2) == null);
        assertTrue(this.getParent(b2) == a2);
        assertTrue(this.getParent(c2) == a2);
        assertTrue(this.getParent(d2) == b2);
        assertTrue(this.getParent(e2) == b2);
        assertTrue(this.getParent(f2) == c2);
        assertTrue(this.getParent(g2) == c2);
        assertTrue(this.getParent(h2) == g2);

    }

    private void breakTree() {

        this.em.createQuery("update TestNode set parentId = null where name='c' and discriminator = 'tree_1'").executeUpdate();
        this.em.createQuery("update TestNode set treeLeft = 0, treeRight = 0, treeLevel = 0 where discriminator = 'tree_1'").executeUpdate();

    }

    private void removeTree() {
        this.em.createQuery("delete from TestNode where discriminator = 'tree_1'").executeUpdate();
    }

}
