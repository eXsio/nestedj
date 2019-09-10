package pl.exsio.nestedj.jpa.discriminator;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import pl.exsio.nestedj.model.NestedNode;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class MapJpaTreeDiscriminator<ID extends Serializable, N extends NestedNode<ID>> implements JpaTreeDiscriminator<ID, N> {

    private Map<String, Supplier<Object>> valueProviders;

    public MapJpaTreeDiscriminator(Map<String, Supplier<Object>> valueProviders) {
        this.valueProviders = valueProviders;
    }

    public MapJpaTreeDiscriminator() {
        this.valueProviders = Maps.newHashMap();
    }

    public void setValueProviders(Map<String, Supplier<Object>> valueProviders) {
        this.valueProviders = valueProviders;
    }

    @Override
    public List<Predicate> getPredicates(CriteriaBuilder cb, Root<N> root) {
        List<Predicate> predicates = Lists.newArrayList();
        for (Map.Entry<String, Supplier<Object>> providerEntry : valueProviders.entrySet()) {
            predicates.add(cb.equal(root.get(providerEntry.getKey()), providerEntry.getValue().get()));
        }
        return predicates;
    }
}
