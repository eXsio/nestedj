package pl.exsio.nestedj.discriminator;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import pl.exsio.nestedj.model.NestedNode;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class MapTreeDiscriminator<ID extends Serializable, N extends NestedNode<ID>> implements TreeDiscriminator<ID, N> {

    private Map<String, ValueProvider> valueProviders;

    public MapTreeDiscriminator(Map<String, ValueProvider> valueProviders) {
        this.valueProviders = valueProviders;
    }

    public MapTreeDiscriminator() {
        this.valueProviders = Maps.newHashMap();
    }

    public void setValueProviders(Map<String, ValueProvider> valueProviders) {
        this.valueProviders = valueProviders;
    }

    @Override
    public List<Predicate> getPredicates(CriteriaBuilder cb, Root<N> root) {
        List<Predicate> predicates = Lists.newArrayList();
        for (Map.Entry<String, ValueProvider> providerEntry : valueProviders.entrySet()) {
            predicates.add(cb.equal(root.get(providerEntry.getKey()), providerEntry.getValue().getDiscriminatorValue()));
        }
        return predicates;
    }
}
