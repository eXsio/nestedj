package pl.exsio.nestedj.delegate.query.jpa;

import com.google.common.base.Preconditions;
import pl.exsio.nestedj.delegate.query.NestedNodeMovingQueryDelegate;
import pl.exsio.nestedj.discriminator.TreeDiscriminator;
import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.model.NestedNodeInfo;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import java.io.Serializable;

import static pl.exsio.nestedj.model.NestedNode.*;

public class JpaNestedNodeMovingQueryDelegateImpl<ID extends Serializable, N extends NestedNode<ID>>
        extends JpaNestedNodeQueryDelegate<ID, N>
        implements NestedNodeMovingQueryDelegate<ID, N> {

    private enum Mode {
        UP, DOWN
    }

    private final static Long MARKING_MODIFIER = 1000L;

    public JpaNestedNodeMovingQueryDelegateImpl(EntityManager entityManager, TreeDiscriminator<ID, N> treeDiscriminator, Class<N> nodeClass, Class<ID> idClass) {
        super(entityManager, treeDiscriminator, nodeClass, idClass);
    }

    @Override
    public Integer markNodeIds(NestedNodeInfo<ID, N> node) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<N> update = cb.createCriteriaUpdate(nodeClass);
        Root<N> root = update.from(node.getNodeClass());
        update
                .set(root.<Long>get(LEFT), doMarking(root, LEFT))
                .set(root.<Long>get(RIGHT), doMarking(root, RIGHT))
                .where(
                        getPredicates(cb, root,
                                cb.greaterThanOrEqualTo(root.get(LEFT), node.getLeft()),
                                cb.lessThanOrEqualTo(root.get(RIGHT), node.getRight())
                        ));
        return entityManager.createQuery(update).executeUpdate();
    }


    @Override
    public void updateFieldsUp(Long delta, Long start, Long stop, String field) {
        updateFields(Mode.UP, delta, start, stop, field);
    }

    @Override
    public void updateFieldsDown(Long delta, Long start, Long stop, String field) {
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
    public void updateParentField(ID newParentId, NestedNodeInfo<ID, N> node) {
        Preconditions.checkNotNull(newParentId);
        doUpdateParentField(newParentId, node);
    }

    @Override
    public void clearParentField(NestedNodeInfo<ID, N> node) {
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
            update.set(root.<Long>get(RIGHT), cb.diff(undoMarking(root, RIGHT), nodeDelta));
            update.set(root.<Long>get(LEFT), cb.diff(undoMarking(root, LEFT), nodeDelta));
        } else if (Mode.UP.equals(mode)) {
            update.set(root.<Long>get(RIGHT), cb.sum(undoMarking(root, RIGHT), nodeDelta));
            update.set(root.<Long>get(LEFT), cb.sum(undoMarking(root, LEFT), nodeDelta));
        }
        update.where(
                getPredicates(cb, root, cb.lessThan(root.get(RIGHT), 0))
        );
        entityManager.createQuery(update).executeUpdate();
    }

    private Expression<Long> doMarking(Root<N> root, String left) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        return cb.diff(cb.neg(root.get(left)), MARKING_MODIFIER);
    }

    private Expression<Long> undoMarking(Root<N> root, String left) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        return cb.neg(cb.sum(root.get(left), MARKING_MODIFIER));
    }

    private void doUpdateParentField(ID newParentId, NestedNodeInfo<ID, N> node) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<N> update = cb.createCriteriaUpdate(node.getNodeClass());
        Root<N> root = update.from(node.getNodeClass());

        update.set(root.get(PARENT_ID), newParentId)
                .where(getPredicates(cb, root, cb.equal(root.get(ID), node.getId())));

        entityManager.createQuery(update).executeUpdate();
    }
}
