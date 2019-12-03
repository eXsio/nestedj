package pl.exsio.nestedj.config.jdbc.factory;

import pl.exsio.nestedj.DelegatingNestedNodeRepository;
import pl.exsio.nestedj.NestedNodeRepository;
import pl.exsio.nestedj.config.jdbc.JdbcNestedNodeRepositoryConfiguration;
import pl.exsio.nestedj.delegate.control.*;
import pl.exsio.nestedj.delegate.query.jdbc.*;
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
