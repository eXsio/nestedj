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

package pl.exsio.nestedj.config.jdbc.factory;

import pl.exsio.nestedj.DelegatingNestedNodeRepository;
import pl.exsio.nestedj.NestedNodeRepository;
import pl.exsio.nestedj.config.jdbc.JdbcNestedNodeRepositoryConfiguration;
import pl.exsio.nestedj.delegate.control.QueryBasedNestedNodeInserter;
import pl.exsio.nestedj.delegate.control.QueryBasedNestedNodeMover;
import pl.exsio.nestedj.delegate.control.QueryBasedNestedNodeRebuilder;
import pl.exsio.nestedj.delegate.control.QueryBasedNestedNodeRemover;
import pl.exsio.nestedj.delegate.control.QueryBasedNestedNodeRetriever;
import pl.exsio.nestedj.delegate.query.jdbc.JdbcNestedNodeInsertingQueryDelegate;
import pl.exsio.nestedj.delegate.query.jdbc.JdbcNestedNodeMovingQueryDelegate;
import pl.exsio.nestedj.delegate.query.jdbc.JdbcNestedNodeRebuildingQueryDelegate;
import pl.exsio.nestedj.delegate.query.jdbc.JdbcNestedNodeRemovingQueryDelegate;
import pl.exsio.nestedj.delegate.query.jdbc.JdbcNestedNodeRetrievingQueryDelegate;
import pl.exsio.nestedj.lock.NoLock;
import pl.exsio.nestedj.model.NestedNode;

import java.io.Serializable;

/**
 * Factory class to construct new instances of JDBC Tree Repositories.
 */
public final class JdbcNestedNodeRepositoryFactory {

    private JdbcNestedNodeRepositoryFactory() {
    }

    /**
     * Creates a new instance of NestedNodeRepository backed by JDBC storage without any Repository locking.
     *
     * @param configuration - JDBC Repository configuration
     * @param <ID> - Nested Node Identifier Class
     * @param <N> - Nested Node Class
     * @return - a new instance of NestedNodeRepository backed by JDBC storage
     */
    public static <ID extends Serializable, N extends NestedNode<ID>> NestedNodeRepository<ID, N> create(JdbcNestedNodeRepositoryConfiguration<ID, N> configuration) {
        return create(configuration, new NoLock<>());
    }

    /**
     * Creates a new instance of NestedNodeRepository backed by JDBC storage with custom Repository locking.
     *
     * @param configuration - JDBC Repository configuration
     * @param lock - custom Repository Lock implementation
     * @param <ID> - Nested Node Identifier Class
     * @param <N> - Nested Node Class
     * @return - a new instance of NestedNodeRepository backed by JDBC storage
     */
    public static <ID extends Serializable, N extends NestedNode<ID>> NestedNodeRepository<ID, N> create(JdbcNestedNodeRepositoryConfiguration<ID, N> configuration, NestedNodeRepository.Lock<ID, N> lock) {
        QueryBasedNestedNodeInserter<ID, N> inserter = new QueryBasedNestedNodeInserter<>(new JdbcNestedNodeInsertingQueryDelegate<>(configuration));
        QueryBasedNestedNodeRetriever<ID, N> retriever = new QueryBasedNestedNodeRetriever<>(new JdbcNestedNodeRetrievingQueryDelegate<>(configuration));
        return new DelegatingNestedNodeRepository<>(
                new QueryBasedNestedNodeMover<>(new JdbcNestedNodeMovingQueryDelegate<>(configuration)),
                new QueryBasedNestedNodeRemover<>(new JdbcNestedNodeRemovingQueryDelegate<>(configuration)),
                retriever,
                new QueryBasedNestedNodeRebuilder<>(inserter, retriever, new JdbcNestedNodeRebuildingQueryDelegate<>(configuration)),
                inserter,
                lock
        );

    }
}
