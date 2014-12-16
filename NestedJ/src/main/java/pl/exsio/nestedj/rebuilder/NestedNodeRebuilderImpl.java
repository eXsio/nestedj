/* 
 * The MIT License
 *
 * Copyright 2014 exsio.
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
package pl.exsio.nestedj.rebuilder;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import pl.exsio.nestedj.NestedNodeInserter;
import pl.exsio.nestedj.NestedNodeMover;
import pl.exsio.nestedj.NestedNodeRebuilder;
import pl.exsio.nestedj.ex.InvalidNodesHierarchyException;
import pl.exsio.nestedj.model.NestedNode;
import static pl.exsio.nestedj.util.NestedNodeUtil.*;

/**
 *
 * @author exsio
 * @param <T>
 */
public class NestedNodeRebuilderImpl implements NestedNodeRebuilder {

    @PersistenceContext
    protected EntityManager em;

    protected NestedNodeInserter inserter;

    protected Class<? extends NestedNode> c;

    public NestedNodeRebuilderImpl() {
    }

    public NestedNodeRebuilderImpl(NestedNodeInserter inserter) {
        this.inserter = inserter;
    }

    public NestedNodeRebuilderImpl(EntityManager em, NestedNodeInserter inserter) {
        this.em = em;
        this.inserter = inserter;
    }

    public NestedNodeRebuilderImpl(EntityManager em) {
        this.em = em;
    }

    public void setInserter(NestedNodeInserter inserter) {
        this.inserter = inserter;
    }

    @Override
    @Transactional
    public void rebuildTree(Class<? extends NestedNode> nodeClass) throws InvalidNodesHierarchyException {
        this.c = nodeClass;
        NestedNode first = this.findFirstNestedNode();
        this.resetFirst(first);
        this.restoreSiblings(first);
        this.rebuildRecursively(first);
        for (NestedNode node : this.getSiblings(first)) {
            this.rebuildRecursively(node);
        }
        this.em.refresh(first);
    }

    protected void rebuildRecursively(NestedNode parent) throws InvalidNodesHierarchyException {
        for (NestedNode child : this.getChildren(parent)) {
            this.inserter.insert(child, parent, NestedNodeMover.MODE_LAST_CHILD);
            this.rebuildRecursively(child);
        }
    }

    protected NestedNode findFirstNestedNode() {

        NestedNode first = (NestedNode) this.em.createQuery("from " + entity(c) + " "
                + "where " + parent(c) + " is null")
                .setMaxResults(1)
                .getSingleResult();
        return first;
    }

    protected void resetFirst(NestedNode first) {
        this.em.createQuery("update  " + entity(c) + " "
                + "set " + left(c) + " = 1, "
                + right(c) + " = 2 "
                + "where " + id(c) + " = :id")
                .setParameter("id", first.getId())
                .executeUpdate();
    }

    protected Iterable<NestedNode> getSiblings(NestedNode first) {
        return this.em.createQuery("from " + entity(c) + " "
                + "where " + parent(c) + " is null "
                + "and " + id(c) + " != :id "
                + "order by " + id(c) + " desc")
                .setParameter("id", first.getId())
                .getResultList();
    }

    protected void restoreSiblings(NestedNode first) throws InvalidNodesHierarchyException {
        for (NestedNode node : this.getSiblings(first)) {
            this.inserter.insert(node, first, NestedNodeMover.MODE_NEXT_SIBLING);
        }
    }

    protected Iterable<NestedNode> getChildren(NestedNode parent) {
        return this.em.createQuery("from " + entity(c) + " "
                + "where " + parent(c) + " = :parent "
                + "order by " + id(c) + " desc")
                .setParameter("parent", parent)
                .getResultList();
    }

    public void setEntityManager(EntityManager em) {
        this.em = em;
    }

}
