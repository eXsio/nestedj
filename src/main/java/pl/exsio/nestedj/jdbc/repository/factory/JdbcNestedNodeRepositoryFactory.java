package pl.exsio.nestedj.jdbc.repository.factory;

import pl.exsio.nestedj.DelegatingNestedNodeRepository;
import pl.exsio.nestedj.NestedNodeRepository;
import pl.exsio.nestedj.config.jdbc.JdbcNestedNodeRepositoryConfiguration;
import pl.exsio.nestedj.delegate.control.*;
import pl.exsio.nestedj.delegate.query.jdbc.*;
import pl.exsio.nestedj.model.NestedNode;

import java.io.Serializable;

public final class JdbcNestedNodeRepositoryFactory {

    private JdbcNestedNodeRepositoryFactory() {
    }

    public static <ID extends Serializable, N extends NestedNode<ID>> NestedNodeRepository<ID, N> create(JdbcNestedNodeRepositoryConfiguration<ID, N> configuration) {
        QueryBasedNestedNodeInserter<ID, N> inserter = new QueryBasedNestedNodeInserter<>(new JdbcNestedNodeInsertingQueryDelegate<>(configuration));
        QueryBasedNestedNodeRetriever<ID, N> retriever = new QueryBasedNestedNodeRetriever<>(new JdbcNestedNodeRetrievingQueryDelegate<>(configuration));
        return new DelegatingNestedNodeRepository<>(
                new QueryBasedNestedNodeMover<>(new JdbcNestedNodeMovingQueryDelegate<>(configuration)),
                new QueryBasedNestedNodeRemover<>(new JdbcNestedNodeRemovingQueryDelegate<>(configuration)),
                retriever,
                new QueryBasedNestedNodeRebuilder<>(inserter, retriever, new JdbcNestedNodeRebuildingQueryDelegate<>(configuration)),
                inserter
        );

    }
}
