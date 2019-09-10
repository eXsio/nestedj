package pl.exsio.nestedj.delegate.query.jpa;

import pl.exsio.nestedj.delegate.query.NestedNodeInsertingQueryDelegate;
import pl.exsio.nestedj.jpa.discriminator.TreeDiscriminator;
import pl.exsio.nestedj.model.NestedNode;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;
import java.io.Serializable;

public class JpaNestedNodeInsertingQueryDelegate<ID extends Serializable, N extends NestedNode<ID>>
        extends JpaNestedNodeQueryDelegate<ID, N>
        implements NestedNodeInsertingQueryDelegate<ID, N> {

    public JpaNestedNodeInsertingQueryDelegate(EntityManager entityManager, TreeDiscriminator<ID, N> treeDiscriminator,
                                               Class<N> nodeClass, Class<ID> idClass) {
        super(entityManager, treeDiscriminator, nodeClass, idClass);
    }

    @Override
    public void insert(N node) {
        entityManager.persist(node);
    }

    @Override
    public void incrementSideFieldsGreaterThan(Long from, String fieldName) {
        updateFields(from, fieldName, false);
    }

    @Override
    public void incermentSideFieldsGreaterThanOrEqualTo(Long from, String fieldName) {
        updateFields(from, fieldName, true);
    }

    private void updateFields(Long from, String fieldName, boolean gte) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<N> update = cb.createCriteriaUpdate(nodeClass);
        Root<N> root = update.from(nodeClass);
        update.set(root.<Long>get(fieldName), cb.sum(root.get(fieldName), INCREMENT_BY));
        if(gte) {
            update.where(getPredicates(cb, root, cb.greaterThanOrEqualTo(root.get(fieldName), from)));
        } else {
            update.where(getPredicates(cb, root, cb.greaterThan(root.get(fieldName), from)));
        }
        entityManager.createQuery(update).executeUpdate();
    }
}
