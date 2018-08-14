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
package pl.exsio.nestedj.delegate.jpa;

import pl.exsio.nestedj.delegate.NestedNodeRetriever;
import pl.exsio.nestedj.discriminator.TreeDiscriminator;
import pl.exsio.nestedj.model.InMemoryTree;
import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.model.NestedNodeInfo;
import pl.exsio.nestedj.model.Tree;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Optional;

import static pl.exsio.nestedj.util.NestedNodeUtil.*;

public class JpaNestedNodeRetriever<ID extends Serializable, N extends NestedNode<ID, N>> extends JpaNestedNodeDelegate<ID, N> implements NestedNodeRetriever<ID, N> {

    public JpaNestedNodeRetriever(EntityManager entityManager, TreeDiscriminator<ID, N> treeDiscriminator) {
        super(entityManager, treeDiscriminator);
    }

    @Override
    public Tree<ID, N> getTree(N node) {
        Tree<ID, N> tree = new InMemoryTree<>(node);
        for (N n : getChildren(node)) {
            Tree<ID, N> subtree = this.getTree(n);
            tree.addChild(subtree);
        }
        return tree;
    }

    @Override
    public Iterable<N> getTreeAsList(N node) {
        Class<N> nodeClass = getNodeClass(node);
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<N> select = cb.createQuery(nodeClass);
        Root<N> root = select.from(nodeClass);
        select.where(getPredicates(cb, root,
                cb.greaterThanOrEqualTo(root.<Long>get(left(nodeClass)), node.getLeft()),
                cb.lessThanOrEqualTo(root.<Long>get(right(nodeClass)), node.getRight())
        )).orderBy(cb.asc(root.<Long>get(left(nodeClass))));

        return entityManager.createQuery(select).getResultList();
    }

    @Override
    public Iterable<N> getChildren(N node) {
        Class<N> nodeClass = getNodeClass(node);
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<N> select = cb.createQuery(nodeClass);
        Root<N> root = select.from(nodeClass);
        select.where(getPredicates(cb, root,
                cb.greaterThanOrEqualTo(root.<Long>get(left(nodeClass)), node.getLeft()),
                cb.lessThanOrEqualTo(root.<Long>get(right(nodeClass)), node.getRight()),
                cb.equal(root.<Long>get(level(nodeClass)), node.getLevel() + 1)
        )).orderBy(cb.asc(root.<Long>get(left(nodeClass))));
        return entityManager.createQuery(select).getResultList();
    }

    @Override
    public Optional<N> getParent(N node) {
        if (node.getLevel() > 0) {
            Class<N> nodeClass = getNodeClass(node);
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<N> select = cb.createQuery(nodeClass);
            Root<N> root = select.from(nodeClass);
            select.where(getPredicates(cb, root,
                    cb.lessThan(root.<Long>get(left(nodeClass)), node.getLeft()),
                    cb.greaterThan(root.<Long>get(right(nodeClass)), node.getRight()),
                    cb.equal(root.<Long>get(level(nodeClass)), node.getLevel() - 1)
            )).orderBy(cb.asc(root.<Long>get(left(nodeClass))));
            return Optional.of(entityManager.createQuery(select).setMaxResults(1).getSingleResult());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Iterable<N> getParents(N node) {
        if (node.getLevel() > 0) {
            Class<N> nodeClass = getNodeClass(node);
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<N> select = cb.createQuery(nodeClass);
            Root<N> root = select.from(nodeClass);
            select.where(getPredicates(cb, root,
                    cb.lessThan(root.<Long>get(left(nodeClass)), node.getLeft()),
                    cb.greaterThan(root.<Long>get(right(nodeClass)), node.getRight())
            )).orderBy(cb.desc(root.<Long>get(left(nodeClass))));
            return entityManager.createQuery(select).getResultList();
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public Optional<NestedNodeInfo<ID, N>> getNodeInfo(ID nodeId, Class<N> nodeClass) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<NestedNodeInfo> select = cb.createQuery(NestedNodeInfo.class);
        Root<N> root = select.from(nodeClass);
        Join<N, N> parent = root.join(parent(nodeClass), JoinType.LEFT);
        select.select(
                cb.construct(
                        NestedNodeInfo.class,
                        root.get(id(nodeClass)),
                        parent.get(id(nodeClass)),
                        root.get(left(nodeClass)),
                        root.get(right(nodeClass)),
                        root.get(level(nodeClass))
                )
        ).where(cb.equal(root.get(id(nodeClass)), nodeId));
        try {
            NestedNodeInfo<ID, N> result = entityManager.createQuery(select).getSingleResult();
            result.setNodeClass(nodeClass);
            return Optional.of(result);
        } catch (NoResultException ex) {
            return Optional.empty();
        }
    }

}
