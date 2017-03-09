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
package pl.exsio.nestedj.delegate;

import pl.exsio.nestedj.discriminator.TreeDiscriminator;
import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.model.NestedNodeInfo;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;

import static pl.exsio.nestedj.util.NestedNodeUtil.left;
import static pl.exsio.nestedj.util.NestedNodeUtil.right;

public class NestedNodeInserterImpl<N extends NestedNode<N>> extends NestedNodeDelegate<N> implements NestedNodeInserter<N> {

    @PersistenceContext
    private EntityManager em;

    public NestedNodeInserterImpl(TreeDiscriminator<N> treeDiscriminator) {
        super(treeDiscriminator);
    }

    public NestedNodeInserterImpl(EntityManager em, TreeDiscriminator<N> treeDiscriminator) {
        super(treeDiscriminator);
        this.em = em;
    }

    @Override
    public void insert(N node, NestedNodeInfo<N> parentInfo, Mode mode) {
        Class<N> nodeClass = getNodeClass(node);
        this.makeSpaceForNewElement(getMoveFrom(parentInfo, mode), mode, nodeClass);
        this.insertNodeIntoTree(parentInfo, node, mode, nodeClass);
    }

    private void insertNodeIntoTree(NestedNodeInfo<N> parent, N node, Mode mode, Class<N> nodeClass) {
        Long left = this.getNodeLeft(parent, mode);
        Long right = left + 1;
        Long level = this.getNodeLevel(parent, mode);
        N nodeParent = this.getNodeParent(parent, mode);

        node.setLeft(left);
        node.setRight(right);
        node.setLevel(level);
        node.setParent(nodeParent);

        em.persist(node);
    }

    private void makeSpaceForNewElement(Long from, Mode mode, Class<N> nodeClass) {
        this.updateFields(from, mode, nodeClass, right(nodeClass));
        this.updateFields(from, mode, nodeClass, left(nodeClass));
    }

    private void updateFields(Long from, Mode mode, Class<N> nodeClass, String fieldName) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaUpdate<N> update = cb.createCriteriaUpdate(nodeClass);
        Root<N> root = update.from(nodeClass);

        update.set(root.<Long>get(fieldName), cb.sum(root.<Long>get(fieldName), 2L));
        if (applyGte(mode)) {
            update.where(getPredicates(cb, root, cb.greaterThanOrEqualTo(root.<Long>get(fieldName), from)));
        } else {
            update.where(getPredicates(cb, root, cb.greaterThan(root.<Long>get(fieldName), from)));
        }
        em.createQuery(update).executeUpdate();
    }

    private Long getMoveFrom(NestedNodeInfo<N> parent, Mode mode) {
        switch (mode) {
            case PREV_SIBLING:
            case FIRST_CHILD:
                return parent.getLeft();
            case NEXT_SIBLING:
            case LAST_CHILD:
            default:
                return parent.getRight();
        }
    }

    private Long getNodeLevel(NestedNodeInfo<N> parent, Mode mode) {
        switch (mode) {
            case NEXT_SIBLING:
            case PREV_SIBLING:
                return parent.getLevel();
            case LAST_CHILD:
            case FIRST_CHILD:
            default:
                return parent.getLevel() + 1;
        }
    }

    private N getNodeParent(NestedNodeInfo<N> parent, Mode mode) {
        switch (mode) {
            case NEXT_SIBLING:
            case PREV_SIBLING:
                if (parent.getParentId() != null) {
                    return em.getReference(parent.getNodeClass(), parent.getParentId());
                } else {
                    return null;
                }
            case LAST_CHILD:
            case FIRST_CHILD:
            default:
                return em.getReference(parent.getNodeClass(), parent.getId());
        }
    }

    private Long getNodeLeft(NestedNodeInfo<N> parent, Mode mode) {
        switch (mode) {
            case NEXT_SIBLING:
                return parent.getRight() + 1;
            case PREV_SIBLING:
                return parent.getLeft();
            case FIRST_CHILD:
                return parent.getLeft() + 1;
            case LAST_CHILD:
            default:
                return parent.getRight();
        }
    }

    private boolean applyGte(Mode mode) {
        switch (mode) {
            case NEXT_SIBLING:
            case FIRST_CHILD:
                return false;
            case PREV_SIBLING:
            case LAST_CHILD:
            default:
                return true;
        }
    }

    public void setEntityManager(EntityManager em) {
        this.em = em;
    }

}
