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
 * @param <ID> - Nested Node Identifier Class
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
