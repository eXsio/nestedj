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
package pl.exsio.nestedj.rebuilder;

import pl.exsio.nestedj.NestedNodeInserter;
import pl.exsio.nestedj.NestedNodeMover;
import pl.exsio.nestedj.NestedNodeRebuilder;
import pl.exsio.nestedj.ex.InvalidNodesHierarchyException;
import pl.exsio.nestedj.model.NestedNode;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.List;

import static pl.exsio.nestedj.util.NestedNodeUtil.id;
import static pl.exsio.nestedj.util.NestedNodeUtil.left;
import static pl.exsio.nestedj.util.NestedNodeUtil.parent;
import static pl.exsio.nestedj.util.NestedNodeUtil.right;

public class NestedNodeRebuilderImpl<N extends NestedNode> implements NestedNodeRebuilder<N> {

    @PersistenceContext
    protected EntityManager em;

    protected NestedNodeInserter<N> inserter;

    public NestedNodeRebuilderImpl() {
    }

    public NestedNodeRebuilderImpl(NestedNodeInserter<N> inserter) {
        this.inserter = inserter;
    }

    public NestedNodeRebuilderImpl(EntityManager em, NestedNodeInserter<N> inserter) {
        this.em = em;
        this.inserter = inserter;
    }

    public NestedNodeRebuilderImpl(EntityManager em) {
        this.em = em;
    }

    public void setInserter(NestedNodeInserter<N> inserter) {
        this.inserter = inserter;
    }

    @Override
    @Transactional
    public void rebuildTree(Class<N> nodeClass) throws InvalidNodesHierarchyException {
        N first = this.findFirstNestedNode(nodeClass);
        this.resetFirst(first, nodeClass);
        this.restoreSiblings(first, nodeClass);
        this.rebuildRecursively(first, nodeClass);
        for (N node : this.getSiblings(first, nodeClass)) {
            this.rebuildRecursively(node, nodeClass);
        }
        this.em.refresh(first);
    }

    protected void rebuildRecursively(N parent, Class<N> nodeClass) throws InvalidNodesHierarchyException {
        for (N child : this.getChildren(parent, nodeClass)) {
            this.inserter.insert(child, parent, NestedNodeMover.MODE_LAST_CHILD);
            this.rebuildRecursively(child, nodeClass);
        }
    }

    protected N findFirstNestedNode(Class<N> nodeClass) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<N> select = cb.createQuery(nodeClass);
        Root<N> from = select.from(nodeClass);
        select.where(cb.isNull(from.get(parent(nodeClass))));
        return em.createQuery(select).setMaxResults(1).getSingleResult();
    }

    protected void resetFirst(N first, Class<N> nodeClass) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaUpdate<N> update = cb.createCriteriaUpdate(nodeClass);
        Root<N> from = update.from(nodeClass);
        update.set(from.get(left(nodeClass)), 1).set(from.get(right(nodeClass)), 2)
                .where(cb.equal(update.getRoot().get(id(nodeClass)), first.getId()));
        em.createQuery(update).executeUpdate();
    }

    protected List<N> getSiblings(N first, Class<N> nodeClass) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<N> select = cb.createQuery(nodeClass);
        Root<N> from = select.from(nodeClass);
        select.where(
                cb.isNull(from.get(parent(nodeClass))),
                cb.notEqual(from.get(id(nodeClass)), first.getId())
        ).orderBy(cb.desc(from.get(id(nodeClass))));
        return em.createQuery(select).getResultList();
    }

    protected void restoreSiblings(N first, Class<N> nodeClass) throws InvalidNodesHierarchyException {
        for (N node : this.getSiblings(first, nodeClass)) {
            this.inserter.insert(node, first, NestedNodeMover.MODE_NEXT_SIBLING);
        }
    }

    protected List<N> getChildren(N parent, Class<N> nodeClass) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<N> select = cb.createQuery(nodeClass);
        Root<N> from = select.from(nodeClass);
        select.where(cb.equal(from.get(parent(nodeClass)), parent)).orderBy(cb.desc(from.get(id(nodeClass))));
        return em.createQuery(select).getResultList();
    }

    public void setEntityManager(EntityManager em) {
        this.em = em;
    }

}
