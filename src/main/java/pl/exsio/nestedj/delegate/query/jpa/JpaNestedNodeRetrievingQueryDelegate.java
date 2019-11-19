package pl.exsio.nestedj.delegate.query.jpa;

import pl.exsio.nestedj.config.jpa.JpaNestedNodeRepositoryConfiguration;
import pl.exsio.nestedj.delegate.query.NestedNodeRetrievingQueryDelegate;
import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.model.NestedNodeInfo;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Optional;

import static pl.exsio.nestedj.model.NestedNode.*;

public class JpaNestedNodeRetrievingQueryDelegate<ID extends Serializable, N extends NestedNode<ID>>
        extends JpaNestedNodeQueryDelegate<ID, N>
        implements NestedNodeRetrievingQueryDelegate<ID, N> {

    private final static Long UPDATE_INCREMENT_BY = 2L;

    public JpaNestedNodeRetrievingQueryDelegate(JpaNestedNodeRepositoryConfiguration<ID, N> configuration) {
        super(configuration);
    }

    @Override
    public Iterable<N> getTreeAsList(N node) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<N> select = cb.createQuery(nodeClass);
        Root<N> root = select.from(nodeClass);
        select.where(getPredicates(cb, root,
                cb.greaterThanOrEqualTo(root.get(LEFT), node.getTreeLeft()),
                cb.lessThanOrEqualTo(root.get(RIGHT), node.getTreeRight())
        )).orderBy(cb.asc(root.<Long>get(LEFT)));

        return entityManager.createQuery(select).getResultList();
    }

    @Override
    public Iterable<N> getChildren(N node) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<N> select = cb.createQuery(nodeClass);
        Root<N> root = select.from(nodeClass);
        select.where(getPredicates(cb, root,
                cb.greaterThanOrEqualTo(root.get(LEFT), node.getTreeLeft()),
                cb.lessThanOrEqualTo(root.get(RIGHT), node.getTreeRight()),
                cb.equal(root.<Long>get(LEVEL), node.getTreeLevel() + 1)
        )).orderBy(cb.asc(root.<Long>get(LEFT)));
        return entityManager.createQuery(select).getResultList();
    }

    @Override
    public Optional<N> getParent(N node) {
        if (node.getTreeLevel() > 0) {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<N> select = cb.createQuery(nodeClass);
            Root<N> root = select.from(nodeClass);
            select.where(getPredicates(cb, root,
                    cb.lessThan(root.<Long>get(LEFT), node.getTreeLeft()),
                    cb.greaterThan(root.<Long>get(RIGHT), node.getTreeRight()),
                    cb.equal(root.<Long>get(LEVEL), node.getTreeLevel() - 1)
            )).orderBy(cb.asc(root.<Long>get(LEFT)));
            return Optional.of(entityManager.createQuery(select).setMaxResults(1).getSingleResult());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Iterable<N> getParents(N node) {
        if (node.getTreeLevel() > 0) {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<N> select = cb.createQuery(nodeClass);
            Root<N> root = select.from(nodeClass);
            select.where(getPredicates(cb, root,
                    cb.lessThan(root.<Long>get(LEFT), node.getTreeLeft()),
                    cb.greaterThan(root.<Long>get(RIGHT), node.getTreeRight())
            )).orderBy(cb.desc(root.<Long>get(LEFT)));
            return entityManager.createQuery(select).getResultList();
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public Optional<N> getPrevSibling(N node) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<N> select = cb.createQuery(nodeClass);
        Root<N> root = select.from(nodeClass);
        select.where(getPredicates(cb, root,
                cb.equal(root.<Long>get(RIGHT), node.getTreeLeft() - 1),
                cb.equal(root.<Long>get(LEVEL), node.getTreeLevel())
        )).orderBy(cb.asc(root.<Long>get(LEFT)));
        try {
            return Optional.of(entityManager.createQuery(select).setMaxResults(1).getSingleResult());
        } catch (NoResultException ex) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<N> getNextSibling(N node) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<N> select = cb.createQuery(nodeClass);
        Root<N> root = select.from(nodeClass);
        select.where(getPredicates(cb, root,
                cb.equal(root.<Long>get(LEFT), node.getTreeRight() + 1),
                cb.equal(root.<Long>get(LEVEL), node.getTreeLevel())
        )).orderBy(cb.asc(root.<Long>get(LEFT)));
        try {
            return Optional.of(entityManager.createQuery(select).setMaxResults(1).getSingleResult());
        } catch (NoResultException ex) {
            return Optional.empty();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<NestedNodeInfo<ID>> getNodeInfo(ID nodeId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<NestedNodeInfo> select = cb.createQuery(NestedNodeInfo.class);
        Root<N> root = select.from(nodeClass);
        select.select(
                cb.construct(
                        NestedNodeInfo.class,
                        root.get(ID),
                        root.get(PARENT_ID),
                        root.get(LEFT),
                        root.get(RIGHT),
                        root.get(LEVEL)
                )
        ).where(cb.equal(root.get(ID), nodeId));
        try {
            return Optional.of(entityManager.createQuery(select).getSingleResult());
        } catch (NoResultException ex) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<N> findFirstRoot() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<N> select = cb.createQuery(nodeClass);
        Root<N> root = select.from(nodeClass);
        select.where(getPredicates(cb, root,
                cb.equal(root.<Long>get(LEVEL), 0L)
        )).orderBy(cb.asc(root.<Long>get(LEFT)));
        try {
            return Optional.of(entityManager.createQuery(select).setMaxResults(1).getSingleResult());
        } catch (NoResultException ex) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<N> findLastRoot() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<N> select = cb.createQuery(nodeClass);
        Root<N> root = select.from(nodeClass);
        select.where(getPredicates(cb, root,
                cb.equal(root.<Long>get(LEVEL), 0L)
        )).orderBy(cb.desc(root.<Long>get(LEFT)));
        try {
            return Optional.of(entityManager.createQuery(select).setMaxResults(1).getSingleResult());
        } catch (NoResultException ex) {
            return Optional.empty();
        }
    }
}
