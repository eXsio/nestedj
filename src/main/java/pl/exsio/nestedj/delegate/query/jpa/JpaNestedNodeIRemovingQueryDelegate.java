package pl.exsio.nestedj.delegate.query.jpa;

import pl.exsio.nestedj.config.jpa.JpaNestedNodeRepositoryConfiguration;
import pl.exsio.nestedj.delegate.query.NestedNodeRemovingQueryDelegate;
import pl.exsio.nestedj.ex.InvalidNodeException;
import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.model.NestedNodeInfo;

import javax.persistence.NoResultException;
import javax.persistence.criteria.*;
import java.io.Serializable;
import java.util.Optional;

import static pl.exsio.nestedj.model.NestedNode.*;

public class JpaNestedNodeIRemovingQueryDelegate<ID extends Serializable, N extends NestedNode<ID>>
        extends JpaNestedNodeQueryDelegate<ID, N>
        implements NestedNodeRemovingQueryDelegate<ID, N> {

    private final static Long UPDATE_INCREMENT_BY = 2L;

    public JpaNestedNodeIRemovingQueryDelegate(JpaNestedNodeRepositoryConfiguration<ID, N> configuration) {
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
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<N> update = cb.createCriteriaUpdate(nodeClass);
        Root<N> root = update.from(nodeClass);

        update.set(root.<Long>get(field), cb.diff(root.get(field), DECREMENT_BY))
                .where(getPredicates(cb, root, cb.greaterThan(root.get(field), from)));

        entityManager.createQuery(update).executeUpdate();
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
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<N> update = cb.createCriteriaUpdate(nodeClass);
        Root<N> root = update.from(nodeClass);

        update.set(root.<Long>get(field), cb.diff(root.get(field), delta))
                .where(getPredicates(cb, root, cb.greaterThan(root.get(field), from)));

        entityManager.createQuery(update).executeUpdate();
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
}
