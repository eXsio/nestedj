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

package pl.exsio.nestedj.config.jpa;

import pl.exsio.nestedj.config.jpa.discriminator.JpaTreeDiscriminator;
import pl.exsio.nestedj.config.jpa.discriminator.MapJpaTreeDiscriminator;
import pl.exsio.nestedj.model.NestedNode;

import javax.persistence.EntityManager;
import java.io.Serializable;

/**
 * Configuration class that serves as a base of creating new instances of JPA Repository.
 *
 * @param <ID> - Nested Node Identifier Class
 * @param <N> - Nested Node Class
 */
public class JpaNestedNodeRepositoryConfiguration<ID extends Serializable, N extends NestedNode<ID>> {

    private final JpaTreeDiscriminator<ID, N> treeDiscriminator;

    private final EntityManager entityManager;

    private final Class<N> nodeClass;

    private final Class<ID> idClass;

    /**
     * Creates new JPA Repository with custom Tree Discriminator.
     *
     * @param entityManager - JPA Entity Manager to be used by the Repository
     * @param nodeClass - Nested Node Identifier Class
     * @param idClass - Nested Node Class
     * @param treeDiscriminator - custom Tree Discriminator
     */
    public JpaNestedNodeRepositoryConfiguration(
            EntityManager entityManager, Class<N> nodeClass, Class<ID> idClass, JpaTreeDiscriminator<ID, N> treeDiscriminator) {
        this.treeDiscriminator = treeDiscriminator;
        this.entityManager = entityManager;
        this.nodeClass = nodeClass;
        this.idClass = idClass;
    }

    /**
     * Creates new JPA Repository with no Tree Discriminator.
     *
     * @param entityManager - JPA Entity Manager to be used by the Repository
     * @param nodeClass - Nested Node Identifier Class
     * @param idClass - Nested Node Class
     */
    public JpaNestedNodeRepositoryConfiguration(EntityManager entityManager, Class<N> nodeClass, Class<ID> idClass) {
        this(entityManager, nodeClass, idClass, new MapJpaTreeDiscriminator<>());
    }

    /**
     * @return Tree Discriminator used by this Configuration
     */
    public JpaTreeDiscriminator<ID, N> getTreeDiscriminator() {
        return treeDiscriminator;
    }

    /**
     * @return JPA Entity Manager used by this Configuration
     */
    public EntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * @return Node Class used by this Configuration
     */
    public Class<N> getNodeClass() {
        return nodeClass;
    }

    /**
     * @return Node Identifier Class used by this Configuration
     */
    public Class<ID> getIdClass() {
        return idClass;
    }
}
