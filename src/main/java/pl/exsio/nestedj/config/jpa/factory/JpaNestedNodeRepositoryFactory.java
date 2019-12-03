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

package pl.exsio.nestedj.config.jpa.factory;

import pl.exsio.nestedj.DelegatingNestedNodeRepository;
import pl.exsio.nestedj.NestedNodeRepository;
import pl.exsio.nestedj.config.jpa.JpaNestedNodeRepositoryConfiguration;
import pl.exsio.nestedj.delegate.control.*;
import pl.exsio.nestedj.delegate.query.jpa.*;
import pl.exsio.nestedj.lock.NoLock;
import pl.exsio.nestedj.model.NestedNode;

import java.io.Serializable;

/**
 * Factory class to construct new instances of JPA Tree Repositories.
 */
public final class JpaNestedNodeRepositoryFactory {

    private JpaNestedNodeRepositoryFactory() {
    }

    /**
     * Creates a new instance of NestedNodeRepository backed by JPA storage without any Repository locking.
     *
     * @param configuration - JPA Repository configuration
     * @param <ID> - Nested Node Identifier Class
     * @param <N> - Nested Node Class
     * @return - a new instance of NestedNodeRepository backed by JPA storage
     */
    public static <ID extends Serializable, N extends NestedNode<ID>> NestedNodeRepository<ID, N> create(JpaNestedNodeRepositoryConfiguration<ID, N> configuration) {
        return create(configuration, new NoLock<>());
    }

    /**
     * Creates a new instance of NestedNodeRepository backed by JPA storage with custom Repository locking.
     *
     * @param configuration - JPA Repository configuration
     * @param lock - custom Repository Lock implementation
     * @param <ID> - Nested Node Identifier Class
     * @param <N> - Nested Node Class
     * @return - a new instance of NestedNodeRepository backed by JPA storage
     */
    public static <ID extends Serializable, N extends NestedNode<ID>> NestedNodeRepository<ID, N> create(JpaNestedNodeRepositoryConfiguration<ID, N> configuration, NestedNodeRepository.Lock<ID, N> lock) {
        QueryBasedNestedNodeInserter<ID, N> inserter = new QueryBasedNestedNodeInserter<>(new JpaNestedNodeInsertingQueryDelegate<>(configuration));
        QueryBasedNestedNodeRetriever<ID, N> retriever = new QueryBasedNestedNodeRetriever<>(new JpaNestedNodeRetrievingQueryDelegate<>(configuration));
        return new DelegatingNestedNodeRepository<>(
                new QueryBasedNestedNodeMover<>(new JpaNestedNodeMovingQueryDelegate<>(configuration)),
                new QueryBasedNestedNodeRemover<>(new JpaNestedNodeRemovingQueryDelegate<>(configuration)),
                retriever,
                new QueryBasedNestedNodeRebuilder<>(inserter, retriever, new JpaNestedNodeRebuildingQueryDelegate<>(configuration)),
                inserter,
                lock
        );
    }
}
