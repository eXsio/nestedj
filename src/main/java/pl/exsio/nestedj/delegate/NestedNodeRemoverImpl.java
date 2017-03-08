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
package pl.exsio.nestedj.delegate;

import pl.exsio.nestedj.discriminator.TreeDiscriminator;
import pl.exsio.nestedj.model.NestedNode;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;

import static pl.exsio.nestedj.util.NestedNodeUtil.left;
import static pl.exsio.nestedj.util.NestedNodeUtil.level;
import static pl.exsio.nestedj.util.NestedNodeUtil.parent;
import static pl.exsio.nestedj.util.NestedNodeUtil.right;

/**
 * @author exsio
 */
public class NestedNodeRemoverImpl<N extends NestedNode<N>> extends NestedNodeDelegate<N> implements NestedNodeRemover<N> {

    @PersistenceContext
    private EntityManager em;

    public NestedNodeRemoverImpl(TreeDiscriminator<N> treeDiscriminator) {
        super(treeDiscriminator);
    }

    public NestedNodeRemoverImpl(EntityManager em, TreeDiscriminator<N> treeDiscriminator) {
        super(treeDiscriminator);
        this.em = em;
    }

    @Override
    public void removeSingle(N node) {
        Class<N> nodeClass = getNodeClass(node);
        Long from = node.getRight();
        N parent = this.findNodeParent(node, nodeClass);
        this.updateNodesParent(node, parent, nodeClass);
        this.prepareTreeForSingleNodeRemoval(from, nodeClass);
        this.updateDeletedNodeChildren(node, nodeClass);
        this.em.remove(node);
        this.em.flush();
        this.em.clear();
    }

    private void prepareTreeForSingleNodeRemoval(Long from, Class<N> nodeClass) {
        this.updateFieldsBeforeSingleNodeRemoval(from, nodeClass, right(nodeClass));
        this.updateFieldsBeforeSingleNodeRemoval(from, nodeClass, left(nodeClass));
    }

    private void updateDeletedNodeChildren(N node, Class<N> nodeClass) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaUpdate<N> update = cb.createCriteriaUpdate(nodeClass);
        Root<N> root = update.from(nodeClass);
        update.set(root.<Long>get(right(nodeClass)), cb.diff(root.<Long>get(right(nodeClass)), 1L))
                .set(root.<Long>get(left(nodeClass)), cb.diff(root.<Long>get(left(nodeClass)), 1L))
                .set(root.<Long>get(level(nodeClass)), cb.diff(root.<Long>get(level(nodeClass)), 1L));

        update.where(getPredicates(cb, root,
                        cb.lessThan(root.<Long>get(right(nodeClass)), node.getRight()),
                        cb.greaterThan(root.<Long>get(left(nodeClass)), node.getLeft()))
        );

        em.createQuery(update).executeUpdate();
    }

    private void updateFieldsBeforeSingleNodeRemoval(Long from, Class<N> nodeClass, String field) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaUpdate<N> update = cb.createCriteriaUpdate(nodeClass);
        Root<N> root = update.from(nodeClass);

        update.set(root.<Long>get(field), cb.diff(root.<Long>get(field), 2L))
                .where(getPredicates(cb, root, cb.greaterThan(root.<Long>get(field), from)));

        em.createQuery(update).executeUpdate();
    }


    private void updateNodesParent(NestedNode node, N parent, Class<N> nodeClass) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaUpdate<N> update = cb.createCriteriaUpdate(nodeClass);
        Root<N> root = update.from(nodeClass);

        update.set(root.get(parent(nodeClass)), parent)
                .where(getPredicates(cb, root,
                        cb.greaterThanOrEqualTo(root.<Long>get(left(nodeClass)), node.getLeft()),
                        cb.lessThanOrEqualTo(root.<Long>get(right(nodeClass)), node.getRight()),
                        cb.equal(root.<Long>get(level(nodeClass)), node.getLevel() + 1)
                ));
        em.createQuery(update).executeUpdate();
    }

    private N findNodeParent(N node, Class<N> nodeClass) {
        if (node.getLevel() > 0) {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<N> select = cb.createQuery(nodeClass);
            Root<N> root = select.from(nodeClass);
            select.where(getPredicates(cb, root,
                    cb.lessThan(root.<Long>get(left(nodeClass)), node.getLeft()),
                    cb.greaterThan(root.<Long>get(right(nodeClass)), node.getRight()),
                    cb.equal(root.<Long>get(level(nodeClass)), node.getLevel() - 1)
            ));
            return em.createQuery(select).setMaxResults(1).getSingleResult();
        }
        return null;
    }

    @Override
    public void removeSubtree(N node) {
        Class<N> nodeClass = getNodeClass(node);
        Long delta = node.getRight() - node.getLeft() + 1;
        Long from = node.getRight();
        this.performBatchDeletion(node, nodeClass);
        this.updateFieldsAfterSubtreeRemoval(from, delta, nodeClass, right(nodeClass));
        this.updateFieldsAfterSubtreeRemoval(from, delta, nodeClass, left(nodeClass));
        this.em.clear();
    }

    private void updateFieldsAfterSubtreeRemoval(Long from, Long delta, Class<N> nodeClass, String field) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaUpdate<N> update = cb.createCriteriaUpdate(nodeClass);
        Root<N> root = update.from(nodeClass);

        update.set(root.<Long>get(field), cb.diff(root.<Long>get(field), delta))
                .where(getPredicates(cb, root, cb.greaterThan(root.<Long>get(field), from)));

        em.createQuery(update).executeUpdate();
    }

    private void performBatchDeletion(N node, Class<N> nodeClass) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaDelete<N> delete = cb.createCriteriaDelete(nodeClass);
        Root<N> root = delete.from(nodeClass);
        delete.where(getPredicates(cb, root,
                cb.greaterThanOrEqualTo(root.<Long>get(left(nodeClass)), node.getLeft()),
                cb.lessThanOrEqualTo(root.<Long>get(right(nodeClass)), node.getRight())
        ));

        em.createQuery(delete).executeUpdate();
    }

    public void setEntityManager(EntityManager em) {
        this.em = em;
    }

}
