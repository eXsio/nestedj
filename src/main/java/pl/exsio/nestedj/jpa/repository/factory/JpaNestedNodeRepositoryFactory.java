package pl.exsio.nestedj.jpa.repository.factory;

import pl.exsio.nestedj.delegate.control.*;
import pl.exsio.nestedj.delegate.query.jpa.*;
import pl.exsio.nestedj.jpa.discriminator.MapTreeDiscriminator;
import pl.exsio.nestedj.jpa.discriminator.TreeDiscriminator;
import pl.exsio.nestedj.jpa.repository.DelegatingNestedNodeRepository;
import pl.exsio.nestedj.jpa.repository.NestedNodeRepository;
import pl.exsio.nestedj.model.NestedNode;

import javax.persistence.EntityManager;
import java.io.Serializable;

public final class JpaNestedNodeRepositoryFactory {

    private JpaNestedNodeRepositoryFactory() {
    }

    public static <ID extends Serializable, N extends NestedNode<ID>> NestedNodeRepository<ID, N> createDefault(Class<ID> idClass, Class<N> nodeClass, EntityManager entityManager) {
        return createDiscriminated(idClass, nodeClass, entityManager, new MapTreeDiscriminator<ID, N>());
    }

    public static <ID extends Serializable, N extends NestedNode<ID>> NestedNodeRepository<ID, N> createDiscriminated(Class<ID> idClass, Class<N> nodeClass, EntityManager entityManager, TreeDiscriminator<ID, N> discriminator) {
        QueryBasedNestedNodeInserter<ID, N> inserter = new QueryBasedNestedNodeInserter<>(new JpaNestedNodeInsertingQueryDelegate<>(entityManager, discriminator, nodeClass, idClass));
        QueryBasedNestedNodeRetriever<ID, N> retriever = new QueryBasedNestedNodeRetriever<>(new JpaNestedNodeRetrievingQueryDelegate<>(entityManager, discriminator, nodeClass, idClass));
        return new DelegatingNestedNodeRepository<>(
                new QueryBasedNestedNodeMover<>(new JpaNestedNodeMovingQueryDelegate<>(entityManager, discriminator, nodeClass, idClass)),
                new QueryBasedNestedNodeRemover<>(new JpaNestedNodeIRemovingQueryDelegate<>(entityManager, discriminator, nodeClass, idClass)),
                retriever,
                new QueryBasedNestedNodeRebuilder<>(inserter, retriever, new JpaNestedNodeRebuildingQueryDelegate<>(entityManager, discriminator, nodeClass, idClass)),
                inserter
        );
    }
}
