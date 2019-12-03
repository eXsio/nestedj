package pl.exsio.nestedj.config.mem.factory;

import pl.exsio.nestedj.DelegatingNestedNodeRepository;
import pl.exsio.nestedj.NestedNodeRepository;
import pl.exsio.nestedj.config.mem.InMemoryNestedNodeRepositoryConfiguration;
import pl.exsio.nestedj.delegate.control.*;
import pl.exsio.nestedj.delegate.query.mem.*;
import pl.exsio.nestedj.lock.NoLock;
import pl.exsio.nestedj.model.NestedNode;

import java.io.Serializable;

/**
 * Factory class to construc new instances of InMemory Tree Repositories.
 */
public final class InMemoryNestedNodeRepositoryFactory {

    private InMemoryNestedNodeRepositoryFactory() {
    }

    /**
     * Creates a new instance of NestedNodeRepository backed by InMemory storage without any Repository locking.
     *
     * @param configuration - InMemory Repository configuration
     * @param <ID> - Nested Node Identier Class
     * @param <N> - Nested Node Class
     * @return - a new instance of NestedNodeRepository backed by InMemory storage
     */
    public static <ID extends Serializable, N extends NestedNode<ID>> NestedNodeRepository<ID, N> create(InMemoryNestedNodeRepositoryConfiguration<ID, N> configuration) {
        return create(configuration, new NoLock<>());
    }

    /**
     * Creates a new instance of NestedNodeRepository backed by InMemory storage with custom Repository locking.
     *
     * @param configuration - InMemory Repository configuration
     * @param lock - custom Repository Lock implementation
     * @param <ID> - Nested Node Identier Class
     * @param <N> - Nested Node Class
     * @return - a new instance of NestedNodeRepository backed by InMemory storage
     */
    public static <ID extends Serializable, N extends NestedNode<ID>> NestedNodeRepository<ID, N> create(InMemoryNestedNodeRepositoryConfiguration<ID, N> configuration, NestedNodeRepository.Lock<ID, N> lock) {
        QueryBasedNestedNodeInserter<ID, N> inserter = new QueryBasedNestedNodeInserter<>(new InMemoryNestedNodeInsertingQueryDelegate<>(configuration));
        QueryBasedNestedNodeRetriever<ID, N> retriever = new QueryBasedNestedNodeRetriever<>(new InMemoryNestedNodeRetrievingQueryDelegate<>(configuration));
        return new DelegatingNestedNodeRepository<>(
                new QueryBasedNestedNodeMover<>(new InMemoryNestedNodeMovingQueryDelegate<>(configuration)),
                new QueryBasedNestedNodeRemover<>(new InMemoryNestedNodeRemovingQueryDelegate<>(configuration)),
                retriever,
                new QueryBasedNestedNodeRebuilder<>(inserter, retriever, new InMemoryNestedNodeRebuildingQueryDelegate<>(configuration)),
                inserter,
                lock
        );
    }
}
