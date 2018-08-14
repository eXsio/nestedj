package pl.exsio.nestedj.discriminator;

import pl.exsio.nestedj.model.NestedNode;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.List;

public interface TreeDiscriminator<ID extends Serializable, N extends NestedNode<ID, N>> {

    List<Predicate> getPredicates(CriteriaBuilder cb, Root<N> root);

    interface ValueProvider {

        Object getDiscriminatorValue();
    }
}
