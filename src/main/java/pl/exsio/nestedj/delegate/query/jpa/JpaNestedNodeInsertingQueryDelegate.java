package pl.exsio.nestedj.delegate.query.jpa;

import pl.exsio.nestedj.delegate.NestedNodeHierarchyManipulator;
import pl.exsio.nestedj.delegate.query.NestedNodeInsertingQueryDelegate;
import pl.exsio.nestedj.discriminator.TreeDiscriminator;
import pl.exsio.nestedj.model.NestedNode;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;
import java.io.Serializable;

import static pl.exsio.nestedj.delegate.NestedNodeHierarchyManipulator.*;

public class JpaNestedNodeInsertingQueryDelegate<ID extends Serializable, N extends NestedNode<ID>>
        extends JpaNestedNodeQueryDelegate<ID, N>
        implements NestedNodeInsertingQueryDelegate<ID, N> {

    public JpaNestedNodeInsertingQueryDelegate(EntityManager entityManager, TreeDiscriminator<ID, N> treeDiscriminator,
                                               Class<N> nodeClass, Class<ID> idClass) {
        super(entityManager, treeDiscriminator, nodeClass, idClass);
    }

    @Override
    public void saveNode(N node) {
        entityManager.persist(node);
    }

    @Override
    public void updateFields(Long from, Mode mode, String fieldName, boolean applyGte) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<N> update = cb.createCriteriaUpdate(nodeClass);
        Root<N> root = update.from(nodeClass);

        update.set(root.<Long>get(fieldName), cb.sum(root.get(fieldName), 2L));
        if (applyGte) {
            update.where(getPredicates(cb, root, cb.greaterThanOrEqualTo(root.get(fieldName), from)));
        } else {
            update.where(getPredicates(cb, root, cb.greaterThan(root.get(fieldName), from)));
        }
        entityManager.createQuery(update).executeUpdate();
    }
}
