package pl.exsio.nestedj.delegate.query.jpa;

import pl.exsio.nestedj.delegate.query.NestedNodeRebuildingQueryDelegate;
import pl.exsio.nestedj.jpa.discriminator.TreeDiscriminator;
import pl.exsio.nestedj.model.NestedNode;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.List;

import static pl.exsio.nestedj.model.NestedNode.*;

public class JpaNestedNodeRebuildingQueryDelegate<ID extends Serializable, N extends NestedNode<ID>>
        extends JpaNestedNodeQueryDelegate<ID, N>
        implements NestedNodeRebuildingQueryDelegate<ID, N> {

    private final static Long UPDATE_INCREMENT_BY = 2L;

    public JpaNestedNodeRebuildingQueryDelegate(EntityManager entityManager, TreeDiscriminator<ID, N> treeDiscriminator,
                                                Class<N> nodeClass, Class<ID> idClass) {
        super(entityManager, treeDiscriminator, nodeClass, idClass);
    }

    @Override
    public void destroyTree() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<N> update = cb.createCriteriaUpdate(nodeClass);
        Root<N> root = update.from(nodeClass);
        update
                .set(root.<Long>get(LEFT), 0L)
                .set(root.<Long>get(RIGHT), 0L)
                .set(root.<Long>get(LEVEL), 0L)
                .where(getPredicates(cb, root));

        entityManager.createQuery(update).executeUpdate();
    }

    @Override
    public N findFirst() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<N> select = cb.createQuery(nodeClass);
        Root<N> root = select.from(nodeClass);
        select.where(getPredicates(cb, root, cb.isNull(root.get(PARENT_ID))))
                .orderBy(cb.desc(root.get(ID)));
        return entityManager.createQuery(select).setMaxResults(1).getSingleResult();
    }

    @Override
    public void resetFirst(N first) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<N> update = cb.createCriteriaUpdate(nodeClass);
        Root<N> root = update.from(nodeClass);
        update.set(root.<Long>get(LEFT), 1L).set(root.<Long>get(RIGHT), 2L)
                .where(getPredicates(cb, root, cb.equal(update.getRoot().get(ID), first.getId())));
        entityManager.createQuery(update).executeUpdate();
    }

    @Override
    public List<N> getSiblings(ID first) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<N> select = cb.createQuery(nodeClass);
        Root<N> root = select.from(nodeClass);
        select.where(getPredicates(cb, root,
                cb.isNull(root.get(PARENT_ID)),
                cb.notEqual(root.get(ID), first)
        )).orderBy(cb.asc(root.get(ID)));
        return entityManager.createQuery(select).getResultList();
    }

    @Override
    public List<N> getChildren(N parent) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<N> select = cb.createQuery(nodeClass);
        Root<N> root = select.from(nodeClass);
        select.where(getPredicates(cb, root, cb.equal(root.get(PARENT_ID), parent.getId()))).orderBy(cb.asc(root.get(ID)));
        return entityManager.createQuery(select).getResultList();
    }
}
