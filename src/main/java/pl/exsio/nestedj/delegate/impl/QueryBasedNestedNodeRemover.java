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
package pl.exsio.nestedj.delegate.impl;

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

import static pl.exsio.nestedj.model.NestedNode.*;

public class QueryBasedNestedNodeRemover<ID extends Serializable, N extends NestedNode<ID>> extends QueryBasedNestedNodeDelegate<ID, N> implements NestedNodeRemover<ID, N> {

    public QueryBasedNestedNodeRemover(EntityManager entityManager, TreeDiscriminator<ID, N> treeDiscriminator) {
        super(entityManager, treeDiscriminator);
    }

    @Override
    public void removeSingle(NestedNodeInfo<ID, N> nodeInfo) {
        Class<N> nodeClass = nodeInfo.getNodeClass();
        Long from = nodeInfo.getRight();
        updateNodesParent(nodeInfo);
        prepareTreeForSingleNodeRemoval(from, nodeClass);
        updateDeletedNodeChildren(nodeInfo);
        performSingleDeletion(nodeInfo);
    }

    private void performSingleDeletion(NestedNodeInfo<ID, N> node) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaDelete<N> delete = cb.createCriteriaDelete(node.getNodeClass());
        Root<N> root = delete.from(node.getNodeClass());
        delete.where(getPredicates(cb, root,
                cb.equal(root.<Long>get(ID), node.getId())
        ));
        entityManager.createQuery(delete).executeUpdate();
    }

    private void prepareTreeForSingleNodeRemoval(Long from, Class<N> nodeClass) {
        updateFieldsBeforeSingleNodeRemoval(from, nodeClass, RIGHT);
        updateFieldsBeforeSingleNodeRemoval(from, nodeClass, LEFT);
    }

    private void updateDeletedNodeChildren(NestedNodeInfo<ID, N> node) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<N> update = cb.createCriteriaUpdate(node.getNodeClass());
        Root<N> root = update.from(node.getNodeClass());
        update.set(root.<Long>get(RIGHT), cb.diff(root.get(RIGHT), 1L))
                .set(root.<Long>get(LEFT), cb.diff(root.get(LEFT), 1L))
                .set(root.<Long>get(LEVEL), cb.diff(root.get(LEVEL), 1L));

        update.where(getPredicates(cb, root,
                        cb.lessThan(root.get(RIGHT), node.getRight()),
                        cb.greaterThan(root.get(LEFT), node.getLeft()))
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


    private void updateNodesParent(NestedNodeInfo<ID, N> node) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<N> update = cb.createCriteriaUpdate(node.getNodeClass());
        Root<N> root = update.from(node.getNodeClass());
        update.set(root.get(PARENT_ID),  findNodeParentId(node).orElse(null))
                .where(getPredicates(cb, root,
                        cb.greaterThanOrEqualTo(root.get(LEFT), node.getLeft()),
                        cb.lessThanOrEqualTo(root.get(RIGHT), node.getRight()),
                        cb.equal(root.<Long>get(LEVEL), node.getLevel() + 1)
                ));
        entityManager.createQuery(update).executeUpdate();
    }

    private Optional<ID> findNodeParentId(NestedNodeInfo<ID, N> node) {
        if (node.getLevel() > 0) {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<ID> select = cb.createQuery(node.getIdClass());
            Root<N> root = select.from(node.getNodeClass());
            select.select(root.get(ID)).where(getPredicates(cb, root,
                    cb.lessThan(root.get(LEFT), node.getLeft()),
                    cb.greaterThan(root.get(RIGHT), node.getRight()),
                    cb.equal(root.<Long>get(LEVEL), node.getLevel() - 1)
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
        Long delta = nodeInfo.getRight() - nodeInfo.getLeft() + 1;
        Long from = nodeInfo.getRight();
        performBatchDeletion(nodeInfo);
        updateFieldsAfterSubtreeRemoval(from, delta, nodeInfo.getNodeClass(), RIGHT);
        updateFieldsAfterSubtreeRemoval(from, delta, nodeInfo.getNodeClass(), LEFT);
    }

    private void updateFieldsAfterSubtreeRemoval(Long from, Long delta, Class<N> nodeClass, String field) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<N> update = cb.createCriteriaUpdate(nodeClass);
        Root<N> root = update.from(nodeClass);

        update.set(root.<Long>get(field), cb.diff(root.get(field), delta))
                .where(getPredicates(cb, root, cb.greaterThan(root.get(field), from)));

        entityManager.createQuery(update).executeUpdate();
    }

    private void performBatchDeletion(NestedNodeInfo<ID, N> node) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaDelete<N> delete = cb.createCriteriaDelete(node.getNodeClass());
        Root<N> root = delete.from(node.getNodeClass());
        delete.where(getPredicates(cb, root,
                cb.greaterThanOrEqualTo(root.get(LEFT), node.getLeft()),
                cb.lessThanOrEqualTo(root.get(RIGHT), node.getRight())
        ));

        entityManager.createQuery(delete).executeUpdate();
    }

}
