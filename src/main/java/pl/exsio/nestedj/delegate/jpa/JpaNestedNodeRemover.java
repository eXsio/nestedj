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
package pl.exsio.nestedj.delegate.jpa;

import pl.exsio.nestedj.delegate.NestedNodeRemover;
import pl.exsio.nestedj.discriminator.TreeDiscriminator;
import pl.exsio.nestedj.ex.InvalidNodeException;
import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.model.NestedNodeInfo;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.*;
import java.io.Serializable;
import java.util.Optional;

import static pl.exsio.nestedj.util.NestedNodeUtil.*;

public class JpaNestedNodeRemover<ID extends Serializable, N extends NestedNode<ID, N>> extends JpaNestedNodeDelegate<ID, N> implements NestedNodeRemover<ID, N> {

    public JpaNestedNodeRemover(EntityManager entityManager, TreeDiscriminator<ID, N> treeDiscriminator) {
        super(entityManager, treeDiscriminator);
    }

    @Override
    public void removeSingle(NestedNodeInfo<ID, N> nodeInfo) {
        Class<N> nodeClass = nodeInfo.getNodeClass();
        Long from = nodeInfo.getRight();
        Optional<N> parent = this.findNodeParent(nodeInfo, nodeClass);
        updateNodesParent(nodeInfo, parent, nodeClass);
        prepareTreeForSingleNodeRemoval(from, nodeClass);
        updateDeletedNodeChildren(nodeInfo, nodeClass);
        performSingleDeletion(nodeInfo, nodeClass);
    }

    private void performSingleDeletion(NestedNodeInfo<ID, N> node, Class<N> nodeClass) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaDelete<N> delete = cb.createCriteriaDelete(nodeClass);
        Root<N> root = delete.from(nodeClass);
        delete.where(getPredicates(cb, root,
                cb.equal(root.<Long>get(id(nodeClass)), node.getId())
        ));
        entityManager.createQuery(delete).executeUpdate();
    }

    private void prepareTreeForSingleNodeRemoval(Long from, Class<N> nodeClass) {
        updateFieldsBeforeSingleNodeRemoval(from, nodeClass, right(nodeClass));
        updateFieldsBeforeSingleNodeRemoval(from, nodeClass, left(nodeClass));
    }

    private void updateDeletedNodeChildren(NestedNodeInfo<ID, N> node, Class<N> nodeClass) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<N> update = cb.createCriteriaUpdate(nodeClass);
        Root<N> root = update.from(nodeClass);
        update.set(root.<Long>get(right(nodeClass)), cb.diff(root.get(right(nodeClass)), 1L))
                .set(root.<Long>get(left(nodeClass)), cb.diff(root.get(left(nodeClass)), 1L))
                .set(root.<Long>get(level(nodeClass)), cb.diff(root.get(level(nodeClass)), 1L));

        update.where(getPredicates(cb, root,
                        cb.lessThan(root.get(right(nodeClass)), node.getRight()),
                        cb.greaterThan(root.get(left(nodeClass)), node.getLeft()))
        );

        entityManager.createQuery(update).executeUpdate();
    }

    private void updateFieldsBeforeSingleNodeRemoval(Long from, Class<N> nodeClass, String field) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<N> update = cb.createCriteriaUpdate(nodeClass);
        Root<N> root = update.from(nodeClass);

        update.set(root.<Long>get(field), cb.diff(root.get(field), 2L))
                .where(getPredicates(cb, root, cb.greaterThan(root.get(field), from)));

        entityManager.createQuery(update).executeUpdate();
    }


    private void updateNodesParent(NestedNodeInfo<ID, N> node, Optional<N> parent, Class<N> nodeClass) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<N> update = cb.createCriteriaUpdate(nodeClass);
        Root<N> root = update.from(nodeClass);
        N newParent = parent.orElse(null);
        update.set(root.get(parent(nodeClass)), newParent)
                .where(getPredicates(cb, root,
                        cb.greaterThanOrEqualTo(root.get(left(nodeClass)), node.getLeft()),
                        cb.lessThanOrEqualTo(root.get(right(nodeClass)), node.getRight()),
                        cb.equal(root.<Long>get(level(nodeClass)), node.getLevel() + 1)
                ));
        entityManager.createQuery(update).executeUpdate();
    }

    private Optional<N> findNodeParent(NestedNodeInfo<ID, N> node, Class<N> nodeClass) {
        if (node.getLevel() > 0) {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<N> select = cb.createQuery(nodeClass);
            Root<N> root = select.from(nodeClass);
            select.where(getPredicates(cb, root,
                    cb.lessThan(root.get(left(nodeClass)), node.getLeft()),
                    cb.greaterThan(root.get(right(nodeClass)), node.getRight()),
                    cb.equal(root.<Long>get(level(nodeClass)), node.getLevel() - 1)
            ));
            try {
                return Optional.of(entityManager.createQuery(select).setMaxResults(1).getSingleResult());
            } catch (NoResultException ex) {
                throw new InvalidNodeException(String.format("Couldn't find node's parent, although its level is greater than 0. It seems the tree is malformed: %s", node));
            }
        }
        return Optional.empty();
    }

    @Override
    public void removeSubtree(NestedNodeInfo<ID, N> nodeInfo) {
        Class<N> nodeClass = nodeInfo.getNodeClass();
        Long delta = nodeInfo.getRight() - nodeInfo.getLeft() + 1;
        Long from = nodeInfo.getRight();
        performBatchDeletion(nodeInfo, nodeClass);
        updateFieldsAfterSubtreeRemoval(from, delta, nodeClass, right(nodeClass));
        updateFieldsAfterSubtreeRemoval(from, delta, nodeClass, left(nodeClass));
    }

    private void updateFieldsAfterSubtreeRemoval(Long from, Long delta, Class<N> nodeClass, String field) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<N> update = cb.createCriteriaUpdate(nodeClass);
        Root<N> root = update.from(nodeClass);

        update.set(root.<Long>get(field), cb.diff(root.get(field), delta))
                .where(getPredicates(cb, root, cb.greaterThan(root.get(field), from)));

        entityManager.createQuery(update).executeUpdate();
    }

    private void performBatchDeletion(NestedNodeInfo<ID, N> node, Class<N> nodeClass) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaDelete<N> delete = cb.createCriteriaDelete(nodeClass);
        Root<N> root = delete.from(nodeClass);
        delete.where(getPredicates(cb, root,
                cb.greaterThanOrEqualTo(root.get(left(nodeClass)), node.getLeft()),
                cb.lessThanOrEqualTo(root.get(right(nodeClass)), node.getRight())
        ));

        entityManager.createQuery(delete).executeUpdate();
    }

}
