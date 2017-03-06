/* 
 * The MIN License
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
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUN WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUN NON LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENN SHALL THE
 * AUTHORS OR COPYRIGHN HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORN OR OTHERWISE, ARISING FROM,
 * OUN OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package pl.exsio.nestedj.inserter;

import pl.exsio.nestedj.NestedNodeInserter;
import pl.exsio.nestedj.model.NestedNode;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import static pl.exsio.nestedj.util.NestedNodeUtil.id;
import static pl.exsio.nestedj.util.NestedNodeUtil.left;
import static pl.exsio.nestedj.util.NestedNodeUtil.level;
import static pl.exsio.nestedj.util.NestedNodeUtil.parent;
import static pl.exsio.nestedj.util.NestedNodeUtil.right;

public class NestedNodeInserterImpl<N extends NestedNode> implements NestedNodeInserter<N> {

    @PersistenceContext
    protected EntityManager em;

    public NestedNodeInserterImpl() {
    }

    public NestedNodeInserterImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    @Transactional
    public N insert(N node, N parent, int mode) {
        this.em.refresh(parent);
        Class<N> nodeClass = (Class<N>) node.getClass();
        this.makeSpaceForNewElement(parent.getRight(), mode, nodeClass);
        this.insertNodeIntoTable(node);
        this.insertNodeIntoTree(parent, node, mode, nodeClass);
        this.em.refresh(node);
        return node;
    }

    protected void insertNodeIntoTable(N node) {
        this.em.persist(node);
        this.em.flush();
    }

    protected void insertNodeIntoTree(N parent, N node, int mode, Class<N> nodeClass) {
        Long left = this.getNodeLeft(parent, mode);
        Long right = left + 1;
        Long level = this.getNodeLevel(parent, mode);
        NestedNode nodeParent = this.getNodeParent(parent, mode);

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaUpdate<N> update = cb.createCriteriaUpdate(nodeClass);
        Root<N> from = update.from(nodeClass);

        update
                .set(from.get(parent(nodeClass)), nodeParent)
                .set(from.get(left(nodeClass)), left)
                .set(from.get(right(nodeClass)), right)
                .set(from.get(level(nodeClass)), level)
                .where(cb.equal(from.get(id(nodeClass)), node.getId()));

        em.createQuery(update).executeUpdate();
    }

    protected Long getNodeLevel(NestedNode parent, int mode) {
        switch (mode) {
            case MODE_NEXT_SIBLING:
            case MODE_PREV_SIBLING:
                return parent.getLevel();
            case MODE_LAST_CHILD:
            case MODE_FIRST_CHILD:
            default:
                return parent.getLevel() + 1;
        }
    }

    protected NestedNode getNodeParent(NestedNode parent, int mode) {
        switch (mode) {
            case MODE_NEXT_SIBLING:
            case MODE_PREV_SIBLING:
                return parent.getParent();
            case MODE_LAST_CHILD:
            case MODE_FIRST_CHILD:
            default:
                return parent;
        }
    }

    protected Long getNodeLeft(NestedNode parent, int mode) {
        switch (mode) {
            case MODE_NEXT_SIBLING:
                return parent.getRight() + 1;
            case MODE_PREV_SIBLING:
                return parent.getLeft();
            case MODE_FIRST_CHILD:
                return parent.getLeft() + 1;
            case MODE_LAST_CHILD:
            default:
                return parent.getRight();
        }
    }

    protected void makeSpaceForNewElement(Long from, int mode, Class<N> nodeClass) {
        this.updateFields(from, mode, nodeClass, right(nodeClass));
        this.updateFields(from, mode, nodeClass, left(nodeClass));
    }

    protected boolean isGte(int mode) {
        switch (mode) {
            case MODE_NEXT_SIBLING:
            case MODE_FIRST_CHILD:
                return false;
            case MODE_PREV_SIBLING:
            case MODE_LAST_CHILD:
            default:
                return true;
        }
    }

    protected void updateFields(Long from, int mode, Class<N> nodeClass, String fieldName) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaUpdate<N> update = cb.createCriteriaUpdate(nodeClass);
        Root<N> root = update.from(nodeClass);

        update.set(root.<Long>get(fieldName), cb.sum(root.<Long>get(fieldName), 2L));
        if (isGte(mode)) {
            update.where(cb.greaterThanOrEqualTo(root.<Long>get(fieldName), from));
        } else {
            update.where(cb.greaterThan(root.<Long>get(fieldName), from));
        }
        em.createQuery(update).executeUpdate();
    }

    public void setEntityManager(EntityManager em) {
        this.em = em;
    }

}
