package pl.exsio.nestedj.config.jpa.discriminator;

import pl.exsio.nestedj.model.NestedNode;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.List;

/**
 * Tree Discriminator for use with JPA Repository implementation.
 * Allows to store multiple intependant Trees in one Repository/Collection.
 *
 * @param <ID> - Nested Node Identier Class
 * @param <N> - Nested Node Class
 */
public interface JpaTreeDiscriminator<ID extends Serializable, N extends NestedNode<ID>> {

    /**
     * Method that decides wich Nodes should be affected by the Criteria Query.
     *
     * @param cb - JPA Criteria Builder
     * @param root - JPA Criteria Root of the Query
     * @return - List of JPA Criteria Predicates to be added to every Criteria Query executed by the JPA implementation
     */
    List<Predicate> getPredicates(CriteriaBuilder cb, Root<N> root);
}
