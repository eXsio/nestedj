package pl.exsio.nestedj.config.mem.factory;

import pl.exsio.nestedj.DelegatingNestedNodeRepository;
import pl.exsio.nestedj.NestedNodeRepository;
import pl.exsio.nestedj.config.mem.InMemoryNestedNodeRepositoryConfiguration;
import pl.exsio.nestedj.delegate.control.*;
import pl.exsio.nestedj.delegate.query.mem.*;
import pl.exsio.nestedj.model.NestedNode;

import java.io.Serializable;

public final class InMemoryNestedNodeRepositoryFactory {

    private InMemoryNestedNodeRepositoryFactory() {
    }


    public static <ID extends Serializable, N extends NestedNode<ID>> NestedNodeRepository<ID, N> create(InMemoryNestedNodeRepositoryConfiguration<ID, N> configuration) {
        QueryBasedNestedNodeInserter<ID, N> inserter = new QueryBasedNestedNodeInserter<>(new InMemoryNestedNodeInsertingQueryDelegate<>(configuration));
        QueryBasedNestedNodeRetriever<ID, N> retriever = new QueryBasedNestedNodeRetriever<>(new InMemoryNestedNodeRetrievingQueryDelegate<>(configuration));
        return new DelegatingNestedNodeRepository<>(
                new QueryBasedNestedNodeMover<>(new InMemoryNestedNodeMovingQueryDelegate<>(configuration)),
                new QueryBasedNestedNodeRemover<>(new InMemoryNestedNodeRemovingQueryDelegate<>(configuration)),
                retriever,
                new QueryBasedNestedNodeRebuilder<>(inserter, retriever, new InMemoryNestedNodeRebuildingQueryDelegate<>(configuration)),
                inserter
        );
    }
}
