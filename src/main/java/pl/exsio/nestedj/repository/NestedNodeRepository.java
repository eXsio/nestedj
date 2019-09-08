/*
 * The MIN License
 *
 * Copyright 2015 exsio.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUN WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUN NON LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENN SHALL THE
 * AUTHORS OR COPYRIGHN HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORN OR OTHERWISE, ARISING FROM,
 * OUN OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package pl.exsio.nestedj.repository;

import pl.exsio.nestedj.delegate.jpa.*;
import pl.exsio.nestedj.delegate.query.jpa.JpaNestedNodeInsertingQueryDelegate;
import pl.exsio.nestedj.delegate.query.jpa.JpaNestedNodeMovingQueryDelegateImpl;
import pl.exsio.nestedj.discriminator.MapTreeDiscriminator;
import pl.exsio.nestedj.discriminator.TreeDiscriminator;
import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.model.Tree;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.Optional;

public interface NestedNodeRepository<ID extends Serializable, N extends NestedNode<ID>> {

    void insertAsFirstChildOf(N node, N parent);

    void insertAsLastChildOf(N node, N parent);

    void insertAsNextSiblingOf(N node, N parent);

    void insertAsPrevSiblingOf(N node, N parent);

    void removeSingle(N node);

    void removeSubtree(N node);

    Iterable<N> getChildren(N node);

    Optional<N> getParent(N node);

    Optional<N> getPrevSibling(N node);

    Optional<N> getNextSibling(N node);

    Iterable<N> getParents(N node);

    Iterable<N> getTreeAsList(N node);

    Tree<ID, N> getTree(N node);

    void rebuildTree();

    void destroyTree();

    static <ID extends Serializable, N extends NestedNode<ID>> NestedNodeRepository<ID, N> createDefault(Class<ID> idClass, Class<N> nodeClass, EntityManager entityManager) {
        return createDiscriminated(idClass, nodeClass, entityManager, new MapTreeDiscriminator<ID, N>());
    }

    static <ID extends Serializable, N extends NestedNode<ID>> NestedNodeRepository<ID, N> createDiscriminated(Class<ID> idClass, Class<N> nodeClass, EntityManager entityManager, TreeDiscriminator<ID, N> discriminator) {
        JpaNestedNodeInserter<ID, N> inserter = new JpaNestedNodeInserter<>(new JpaNestedNodeInsertingQueryDelegate<>(entityManager, discriminator, nodeClass, idClass));
        JpaNestedNodeRetriever<ID, N> retriever = new JpaNestedNodeRetriever<>(entityManager, discriminator);
        return new DelegatingNestedNodeRepository<>(
                idClass,
                nodeClass,
                new JpaNestedNodeMover<>(new JpaNestedNodeMovingQueryDelegateImpl<>(entityManager, discriminator, nodeClass, idClass)),
                new JpaNestedNodeRemover<>(entityManager, discriminator),
                retriever,
                new JpaNestedNodeRebuilder<>(entityManager, discriminator, inserter, retriever),
                inserter
        );
    }

}
