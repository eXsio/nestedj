package pl.exsio.nestedj.config.jpa.discriminator;

import pl.exsio.nestedj.model.NestedNode;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.List;

public interface JpaTreeDiscriminator<ID extends Serializable, N extends NestedNode<ID>> {

    List<Predicate> getPredicates(CriteriaBuilder cb, Root<N> root);
}
