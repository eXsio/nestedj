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

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import pl.exsio.nestedj.config.jpa.JpaNestedNodeRepositoryConfiguration;
import pl.exsio.nestedj.delegate.query.NestedNodeMovingQueryDelegate;
import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.model.NestedNodeInfo;

import java.io.Serializable;

import static pl.exsio.nestedj.model.NestedNode.ID;
import static pl.exsio.nestedj.model.NestedNode.LEFT;
import static pl.exsio.nestedj.model.NestedNode.LEVEL;
import static pl.exsio.nestedj.model.NestedNode.PARENT_ID;
import static pl.exsio.nestedj.model.NestedNode.RIGHT;

public class JpaNestedNodeMovingQueryDelegate<ID extends Serializable, N extends NestedNode<ID>>
        extends JpaNestedNodeQueryDelegate<ID, N>
        implements NestedNodeMovingQueryDelegate<ID, N> {

    private enum Mode {
        UP, DOWN
    }

    private final static Long MARKING_MODIFIER = 1000L;

    public JpaNestedNodeMovingQueryDelegate(JpaNestedNodeRepositoryConfiguration<ID, N> configuration) {
        super(configuration);
    }

    @Override
    public Integer markNodeIds(NestedNodeInfo<ID> node) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<N> update = cb.createCriteriaUpdate(nodeClass);
        Root<N> root = update.from(nodeClass);
        update
                .set(root.<Long>get(RIGHT), markRightField(root))
                .where(
                        getPredicates(cb, root,
                                cb.greaterThanOrEqualTo(root.get(LEFT), node.getLeft()),
                                cb.lessThanOrEqualTo(root.get(RIGHT), node.getRight())
                        ));
        return entityManager.createQuery(update).executeUpdate();
    }


    @Override
    public void updateSideFieldsUp(Long delta, Long start, Long stop, String field) {
        updateFields(Mode.UP, delta, start, stop, field);
    }

    @Override
    public void updateSideFieldsDown(Long delta, Long start, Long stop, String field) {
        updateFields(Mode.DOWN, delta, start, stop, field);
    }

    @Override
    public void performMoveUp(Long nodeDelta, Long levelModificator) {
        performMove(Mode.UP, nodeDelta, levelModificator);
    }

    @Override
    public void performMoveDown(Long nodeDelta, Long levelModificator) {
        performMove(Mode.DOWN, nodeDelta, levelModificator);
    }

    @Override
    public void updateParentField(ID newParentId, NestedNodeInfo<ID> node) {
        if (newParentId == null) {
            throw new NullPointerException("newParentId cannot be null");
        }
        doUpdateParentField(newParentId, node);
    }

    @Override
    public void clearParentField(NestedNodeInfo<ID> node) {
        doUpdateParentField(null, node);
    }

    private void updateFields(Mode mode, Long delta, Long start, Long stop, String field) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<N> update = cb.createCriteriaUpdate(nodeClass);
        Root<N> root = update.from(nodeClass);

        if (Mode.DOWN.equals(mode)) {
            update.set(root.<Long>get(field), cb.diff(root.get(field), delta));
        } else if (Mode.UP.equals(mode)) {
            update.set(root.<Long>get(field), cb.sum(root.get(field), delta));
        }
        update.where(getPredicates(cb, root,
                cb.greaterThan(root.get(field), start),
                cb.lessThan(root.get(field), stop)
        ));
        entityManager.createQuery(update).executeUpdate();
    }

    private void performMove(Mode mode, Long nodeDelta, Long levelModificator) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<N> update = cb.createCriteriaUpdate(nodeClass);
        Root<N> root = update.from(nodeClass);

        update.set(root.<Long>get(LEVEL), cb.sum(root.get(LEVEL), levelModificator));
        if (Mode.DOWN.equals(mode)) {
            update.set(root.<Long>get(RIGHT), cb.diff(unMarkRightField(root), nodeDelta));
            update.set(root.<Long>get(LEFT), cb.diff(root.get(LEFT), nodeDelta));
        } else if (Mode.UP.equals(mode)) {
            update.set(root.<Long>get(RIGHT), cb.sum(unMarkRightField(root), nodeDelta));
            update.set(root.<Long>get(LEFT), cb.sum(root.get(LEFT), nodeDelta));
        }
        update.where(
                getPredicates(cb, root, cb.lessThan(root.get(RIGHT), 0))
        );
        entityManager.createQuery(update).executeUpdate();
    }

    private Expression<Long> markRightField(Root<N> root) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        return cb.diff(cb.neg(root.get(RIGHT)), MARKING_MODIFIER);
    }

    private Expression<Long> unMarkRightField(Root<N> root) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        return cb.neg(cb.sum(root.get(RIGHT), MARKING_MODIFIER));
    }

    private void doUpdateParentField(ID newParentId, NestedNodeInfo<ID> node) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<N> update = cb.createCriteriaUpdate(nodeClass);
        Root<N> root = update.from(nodeClass);

        update.set(root.get(PARENT_ID), newParentId)
                .where(getPredicates(cb, root, cb.equal(root.get(ID), node.getId())));

        entityManager.createQuery(update).executeUpdate();
    }
}
