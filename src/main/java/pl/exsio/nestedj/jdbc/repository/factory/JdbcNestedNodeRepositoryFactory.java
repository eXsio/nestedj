package pl.exsio.nestedj.jdbc.repository.factory;

import pl.exsio.nestedj.NestedNodeRepository;
import pl.exsio.nestedj.config.jdbc.JdbcNestedNodeRepositoryConfiguration;
import pl.exsio.nestedj.model.NestedNode;

import java.io.Serializable;

public final class JdbcNestedNodeRepositoryFactory {

    private JdbcNestedNodeRepositoryFactory() {
    }


    public static <ID extends Serializable, N extends NestedNode<ID>> NestedNodeRepository<ID, N> create(JdbcNestedNodeRepositoryConfiguration<ID, N> configuration) {
//        QueryBasedNestedNodeInserter<ID, N> inserter = new QueryBasedNestedNodeInserter<>(new JpaNestedNodeInsertingQueryDelegate<>(configuration));
//        QueryBasedNestedNodeRetriever<ID, N> retriever = new QueryBasedNestedNodeRetriever<>(new JpaNestedNodeRetrievingQueryDelegate<>(configuration));
//        return new DelegatingNestedNodeRepository<>(
//                new QueryBasedNestedNodeMover<>(new JpaNestedNodeMovingQueryDelegate<>(configuration)),
//                new QueryBasedNestedNodeRemover<>(new JpaNestedNodeIRemovingQueryDelegate<>(configuration)),
//                retriever,
//                new QueryBasedNestedNodeRebuilder<>(inserter, retriever, new JpaNestedNodeRebuildingQueryDelegate<>(configuration)),
//                inserter
//        );
        throw new UnsupportedOperationException();
    }
}
