package pl.exsio.nestedj.delegate.query.jpa;

import pl.exsio.nestedj.config.jpa.JpaNestedNodeRepositoryConfiguration;
import pl.exsio.nestedj.config.jpa.discriminator.JpaTreeDiscriminator;
import pl.exsio.nestedj.model.NestedNode;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class JpaNestedNodeQueryDelegate<ID extends Serializable, N extends NestedNode<ID>> {

    private final JpaTreeDiscriminator<ID, N> treeDiscriminator;

    protected final EntityManager entityManager;

    protected final Class<N> nodeClass;

    protected final Class<ID> idClass;

    public JpaNestedNodeQueryDelegate(JpaNestedNodeRepositoryConfiguration<ID, N> configuration) {
        this.entityManager = configuration.getEntityManager();
        this.treeDiscriminator = configuration.getTreeDiscriminator();
        this.nodeClass = configuration.getNodeClass();
        this.idClass = configuration.getIdClass();
    }

    protected Predicate[] getPredicates(CriteriaBuilder cb, Root<N> root, Predicate... predicates) {
        List<Predicate> predicateList = new ArrayList<>(treeDiscriminator.getPredicates(cb, root));
        Collections.addAll(predicateList, predicates);
        return predicateList.toArray(new Predicate[0]);
    }
}
