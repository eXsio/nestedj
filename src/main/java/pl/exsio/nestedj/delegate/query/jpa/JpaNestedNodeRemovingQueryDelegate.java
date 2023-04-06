/*
 *  The MIT License
 *
 *  Copyright (c) 2019 eXsio.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 *  documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 *  rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 *  permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *  The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 *  the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 *  BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 *  CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 *  ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package pl.exsio.nestedj.delegate.query.jpa;

import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Root;
import pl.exsio.nestedj.config.jpa.JpaNestedNodeRepositoryConfiguration;
import pl.exsio.nestedj.delegate.query.NestedNodeRemovingQueryDelegate;
import pl.exsio.nestedj.ex.InvalidNodeException;
import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.model.NestedNodeInfo;

import java.io.Serializable;
import java.util.Optional;

import static pl.exsio.nestedj.model.NestedNode.ID;
import static pl.exsio.nestedj.model.NestedNode.LEFT;
import static pl.exsio.nestedj.model.NestedNode.LEVEL;
import static pl.exsio.nestedj.model.NestedNode.PARENT_ID;
import static pl.exsio.nestedj.model.NestedNode.RIGHT;

public class JpaNestedNodeRemovingQueryDelegate<ID extends Serializable, N extends NestedNode<ID>>
        extends JpaNestedNodeQueryDelegate<ID, N>
        implements NestedNodeRemovingQueryDelegate<ID, N> {

    private final static Long UPDATE_INCREMENT_BY = 2L;

    public JpaNestedNodeRemovingQueryDelegate(JpaNestedNodeRepositoryConfiguration<ID, N> configuration) {
        super(configuration);
    }

    @Override
    public void setNewParentForDeletedNodesChildren(NestedNodeInfo<ID> node) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<N> update = cb.createCriteriaUpdate(nodeClass);
        Root<N> root = update.from(nodeClass);
        update.set(root.get(PARENT_ID),  findNodeParentId(node).orElse(null))
                .where(getPredicates(cb, root,
                        cb.greaterThanOrEqualTo(root.get(LEFT), node.getLeft()),
                        cb.lessThanOrEqualTo(root.get(RIGHT), node.getRight()),
                        cb.equal(root.<Long>get(LEVEL), node.getLevel() + 1)
                ));
        entityManager.createQuery(update).executeUpdate();
    }

    @Override
    public void performSingleDeletion(NestedNodeInfo<ID> node) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaDelete<N> delete = cb.createCriteriaDelete(nodeClass);
        Root<N> root = delete.from(nodeClass);
        delete.where(getPredicates(cb, root,
                cb.equal(root.<Long>get(ID), node.getId())
        ));
        entityManager.createQuery(delete).executeUpdate();
    }

    private Optional<ID> findNodeParentId(NestedNodeInfo<ID> node) {
        if (node.getLevel() > 0) {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<ID> select = cb.createQuery(idClass);
            Root<N> root = select.from(nodeClass);
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
    public void decrementSideFieldsBeforeSingleNodeRemoval(Long from, String field) {
        decrementSideFields(from, DECREMENT_BY, field);
    }

    @Override
    public void pushUpDeletedNodesChildren(NestedNodeInfo<ID> node) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<N> update = cb.createCriteriaUpdate(nodeClass);
        Root<N> root = update.from(nodeClass);
        update.set(root.<Long>get(RIGHT), cb.diff(root.get(RIGHT), 1L))
                .set(root.<Long>get(LEFT), cb.diff(root.get(LEFT), 1L))
                .set(root.<Long>get(LEVEL), cb.diff(root.get(LEVEL), 1L));

        update.where(getPredicates(cb, root,
                cb.lessThan(root.get(RIGHT), node.getRight()),
                cb.greaterThan(root.get(LEFT), node.getLeft()))
        );

        entityManager.createQuery(update).executeUpdate();
    }

    @Override
    public void decrementSideFieldsAfterSubtreeRemoval(Long from, Long delta, String field) {
        decrementSideFields(from, delta, field);
    }

    @Override
    public void performBatchDeletion(NestedNodeInfo<ID> node) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaDelete<N> delete = cb.createCriteriaDelete(nodeClass);
        Root<N> root = delete.from(nodeClass);
        delete.where(getPredicates(cb, root,
                cb.greaterThanOrEqualTo(root.get(LEFT), node.getLeft()),
                cb.lessThanOrEqualTo(root.get(RIGHT), node.getRight())
        ));

        entityManager.createQuery(delete).executeUpdate();
    }

    private void decrementSideFields(Long from, Long delta, String field) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<N> update = cb.createCriteriaUpdate(nodeClass);
        Root<N> root = update.from(nodeClass);

        update.set(root.<Long>get(field), cb.diff(root.get(field), delta))
                .where(getPredicates(cb, root, cb.greaterThan(root.get(field), from)));

        entityManager.createQuery(update).executeUpdate();
    }
}
