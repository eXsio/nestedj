/*
 *  The MIT License
 *
 *  Copyright (c) 2019 eXsio.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 *  documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 *  rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 *  permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *  The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 *  the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 *  BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 *  CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 *  ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package pl.exsio.nestedj.config.jpa.discriminator;

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

/**
 * Default implementation of JPA Tree Discriminator based on Map of <String, Supplier<Object>>
 *
 * @param <ID> - Nested Node Identifier Class
 * @param <N> - Nested Node Class
 */
public class MapJpaTreeDiscriminator<ID extends Serializable, N extends NestedNode<ID>> implements JpaTreeDiscriminator<ID, N> {

    private Map<String, Supplier<Object>> valueProviders;

    /**
     * Creates a JPA Tree Discriminator that will add Predicates to all the Criteria Queries based on the passed Map.
     * Keys of the Map will be added as Keys in the Criteria Predicates.
     * Values obtained by the Suppliers will be added as Values of corresponding Keys in the Criteria Predicates.
     *
     * @param valueProviders - Map of <String, Supplier<Object>>
     */
    public MapJpaTreeDiscriminator(Map<String, Supplier<Object>> valueProviders) {
        this.valueProviders = valueProviders;
    }

    /**
     * Create no-up Tree Discriminator
     */
    public MapJpaTreeDiscriminator() {
        this.valueProviders = Maps.newHashMap();
    }

    /**
     * Creates a JPA Tree Discriminator that will add Predicates to all the Criteria Queries based on the passed Map.
     * Keys of the Map will be added as Keys in the Criteria Predicates.
     * Values obtained by the Suppliers will be added as Values of corresponding Keys in the Criteria Predicates.
     *
     * @param valueProviders - Map of <String, Supplier<Object>>
     */
    public void setValueProviders(Map<String, Supplier<Object>> valueProviders) {
        this.valueProviders = valueProviders;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Predicate> getPredicates(CriteriaBuilder cb, Root<N> root) {
        List<Predicate> predicates = Lists.newArrayList();
        for (Map.Entry<String, Supplier<Object>> providerEntry : valueProviders.entrySet()) {
            predicates.add(cb.equal(root.get(providerEntry.getKey()), providerEntry.getValue().get()));
        }
        return predicates;
    }
}
