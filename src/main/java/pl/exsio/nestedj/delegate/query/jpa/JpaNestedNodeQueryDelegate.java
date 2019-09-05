package pl.exsio.nestedj.delegate.query.jpa;

import pl.exsio.nestedj.discriminator.TreeDiscriminator;
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

    private final TreeDiscriminator<ID, N> treeDiscriminator;

    protected final EntityManager entityManager;

    protected final Class<N> nodeClass;

    protected final Class<ID> idClass;

    public JpaNestedNodeQueryDelegate(EntityManager entityManager, TreeDiscriminator<ID, N> treeDiscriminator,
                                      Class<N> nodeClass, Class<ID> idClass) {
        this.entityManager = entityManager;
        this.treeDiscriminator = treeDiscriminator;
        this.nodeClass = nodeClass;
        this.idClass = idClass;
    }

    protected Predicate[] getPredicates(CriteriaBuilder cb, Root<N> root, Predicate... predicates) {
        List<Predicate> predicateList = new ArrayList<>(treeDiscriminator.getPredicates(cb, root));
        Collections.addAll(predicateList, predicates);
        return predicateList.toArray(new Predicate[predicateList.size()]);
    }
}
