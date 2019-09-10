package pl.exsio.nestedj.repository.factory;

import pl.exsio.nestedj.delegate.impl.*;
import pl.exsio.nestedj.delegate.query.jpa.JpaNestedNodeIRemovingQueryDelegate;
import pl.exsio.nestedj.delegate.query.jpa.JpaNestedNodeInsertingQueryDelegate;
import pl.exsio.nestedj.delegate.query.jpa.JpaNestedNodeMovingQueryDelegate;
import pl.exsio.nestedj.discriminator.MapTreeDiscriminator;
import pl.exsio.nestedj.discriminator.TreeDiscriminator;
import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.repository.DelegatingNestedNodeRepository;
import pl.exsio.nestedj.repository.NestedNodeRepository;

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
        QueryBasedNestedNodeRetriever<ID, N> retriever = new QueryBasedNestedNodeRetriever<>(entityManager, discriminator);
        return new DelegatingNestedNodeRepository<>(
                idClass,
                nodeClass,
                new QueryBasedNestedNodeMover<>(new JpaNestedNodeMovingQueryDelegate<>(entityManager, discriminator, nodeClass, idClass)),
                new QueryBasedNestedNodeRemover<>(new JpaNestedNodeIRemovingQueryDelegate<>(entityManager, discriminator, nodeClass, idClass)),
                retriever,
                new QueryBasedNestedNodeRebuilder<>(entityManager, discriminator, inserter, retriever),
                inserter
        );
    }
}
