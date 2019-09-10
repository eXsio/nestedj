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
package pl.exsio.nestedj.jpa;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import pl.exsio.nestedj.DelegatingNestedNodeRepository;
import pl.exsio.nestedj.TestConfiguration;
import pl.exsio.nestedj.model.TestNode;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfiguration.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public abstract class FunctionalNestedjTest {

    @Autowired
    protected DelegatingNestedNodeRepository<Long, TestNode> nodeRepository;

    @PersistenceContext
    protected EntityManager em;

    /**
     * STARTING NESTED TREE CONDITIONS
     * <p/>
     *                  1 A 16
     *                   / \                    IDS:
     *                  /   \                   A: 1
     *                 /     \                  B: 2
     *              2 B 7   8 C 15              C: 3
     *               /         \                D: 4
     *              /\         /\               E: 5
     *             /  \       /  \              F: 6
     *            /    \     /    \             G: 7
     *           /   5 E 6  9 F 10 \            H: 8
     *        3 D 4             11 G 14
     *                              \
     *                               \
     *                             12 H 13
     */
    protected TestNode findNode(String symbol) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<TestNode> select = cb.createQuery(TestNode.class);
        Root<TestNode> root = select.from(TestNode.class);
        select.where(cb.equal(root.get("name"), symbol));
        TestNode n = em.createQuery(select).getSingleResult();
        printNode(symbol, n);
        this.em.refresh(n);
        return n;
    }

    protected void printNode(String symbol, TestNode n) {
        if(n != null) {
            System.out.println(String.format("Node %s: %d/%d/%d", symbol, n.getTreeLeft(), n.getTreeRight(), n.getTreeLevel()));
        }
    }

    protected TestNode createTestNode(String symbol) {

        TestNode n = new TestNode();
        n.setName(symbol);
        n.setDiscriminator("tree_1");
        return n;
    }

    protected TestNode getParent(TestNode f) {
        this.em.refresh(f);
        TestNode parent = null;
        Long parentId = f.getParentId();
        if (parentId != null) {
            parent = em.find(TestNode.class, parentId);
        }
        System.out.println(String.format("Parent of %s is %s", f.getName(), parent != null ? parent.getName() : "null"));
        return parent;
    }


    protected void assertSecondTreeIntact() {
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

    protected void breakTree() {

        this.em.createQuery("update TestNode set parentId = null where name='c' and discriminator = 'tree_1'").executeUpdate();
        this.em.createQuery("update TestNode set treeLeft = 0, treeRight = 0, treeLevel = 0 where discriminator = 'tree_1'").executeUpdate();

    }

    protected void removeTree() {
        this.em.createQuery("delete from TestNode where discriminator = 'tree_1'").executeUpdate();
    }

}
