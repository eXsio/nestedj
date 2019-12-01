package pl.exsio.nestedj.config.jpa;

import pl.exsio.nestedj.config.jpa.discriminator.JpaTreeDiscriminator;
import pl.exsio.nestedj.config.jpa.discriminator.MapJpaTreeDiscriminator;
import pl.exsio.nestedj.model.NestedNode;

import javax.persistence.EntityManager;
import java.io.Serializable;

public class JpaNestedNodeRepositoryConfiguration<ID extends Serializable, N extends NestedNode<ID>> {

    private final JpaTreeDiscriminator<ID, N> treeDiscriminator;

    protected final EntityManager entityManager;

    protected final Class<N> nodeClass;

    protected final Class<ID> idClass;

    public JpaNestedNodeRepositoryConfiguration(
            EntityManager entityManager, Class<N> nodeClass, Class<ID> idClass, JpaTreeDiscriminator<ID, N> treeDiscriminator) {
        this.treeDiscriminator = treeDiscriminator;
        this.entityManager = entityManager;
        this.nodeClass = nodeClass;
        this.idClass = idClass;
    }

    public JpaNestedNodeRepositoryConfiguration(EntityManager entityManager, Class<N> nodeClass, Class<ID> idClass) {
        this(entityManager, nodeClass, idClass, new MapJpaTreeDiscriminator<>());
    }

    public JpaTreeDiscriminator<ID, N> getTreeDiscriminator() {
        return treeDiscriminator;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public Class<N> getNodeClass() {
        return nodeClass;
    }

    public Class<ID> getIdClass() {
        return idClass;
    }
}
