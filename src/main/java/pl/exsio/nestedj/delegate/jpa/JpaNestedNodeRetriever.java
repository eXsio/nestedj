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
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Optional;

import static pl.exsio.nestedj.model.NestedNode.*;

public class JpaNestedNodeRetriever<ID extends Serializable, N extends NestedNode<ID>> extends JpaNestedNodeDelegate<ID, N> implements NestedNodeRetriever<ID, N> {

    public JpaNestedNodeRetriever(EntityManager entityManager, TreeDiscriminator<ID, N> treeDiscriminator) {
        super(entityManager, treeDiscriminator);
    }

    @Override
    public Tree<ID, N> getTree(N node, Class<N> nodeClass) {
        Tree<ID, N> tree = new InMemoryTree<>(node);
        for (N n : getChildren(node, nodeClass)) {
            Tree<ID, N> subtree = this.getTree(n, nodeClass);
            tree.addChild(subtree);
        }
        return tree;
    }

    @Override
    public Iterable<N> getTreeAsList(N node, Class<N> nodeClass) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<N> select = cb.createQuery(nodeClass);
        Root<N> root = select.from(nodeClass);
        select.where(getPredicates(cb, root,
                cb.greaterThanOrEqualTo(root.get(LEFT), node.getTreeLeft()),
                cb.lessThanOrEqualTo(root.get(RIGHT), node.getTreeRight())
        )).orderBy(cb.asc(root.<Long>get(LEFT)));

        return entityManager.createQuery(select).getResultList();
    }

    @Override
    public Iterable<N> getChildren(N node, Class<N> nodeClass) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<N> select = cb.createQuery(nodeClass);
        Root<N> root = select.from(nodeClass);
        select.where(getPredicates(cb, root,
                cb.greaterThanOrEqualTo(root.get(LEFT), node.getTreeLeft()),
                cb.lessThanOrEqualTo(root.get(RIGHT), node.getTreeRight()),
                cb.equal(root.<Long>get(LEVEL), node.getTreeLevel() + 1)
        )).orderBy(cb.asc(root.<Long>get(LEFT)));
        return entityManager.createQuery(select).getResultList();
    }

    @Override
    public Optional<N> getParent(N node, Class<N> nodeClass) {
        if (node.getTreeLevel() > 0) {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<N> select = cb.createQuery(nodeClass);
            Root<N> root = select.from(nodeClass);
            select.where(getPredicates(cb, root,
                    cb.lessThan(root.<Long>get(LEFT), node.getTreeLeft()),
                    cb.greaterThan(root.<Long>get(RIGHT), node.getTreeRight()),
                    cb.equal(root.<Long>get(LEVEL), node.getTreeLevel() - 1)
            )).orderBy(cb.asc(root.<Long>get(LEFT)));
            return Optional.of(entityManager.createQuery(select).setMaxResults(1).getSingleResult());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Iterable<N> getParents(N node, Class<N> nodeClass) {
        if (node.getTreeLevel() > 0) {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<N> select = cb.createQuery(nodeClass);
            Root<N> root = select.from(nodeClass);
            select.where(getPredicates(cb, root,
                    cb.lessThan(root.<Long>get(LEFT), node.getTreeLeft()),
                    cb.greaterThan(root.<Long>get(RIGHT), node.getTreeRight())
            )).orderBy(cb.desc(root.<Long>get(LEFT)));
            return entityManager.createQuery(select).getResultList();
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public Optional<N> getPrevSibling(N node, Class<N> nodeClass) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<N> select = cb.createQuery(nodeClass);
        Root<N> root = select.from(nodeClass);
        select.where(getPredicates(cb, root,
            cb.equal(root.<Long>get(RIGHT), node.getTreeLeft() - 1),
            cb.equal(root.<Long>get(LEVEL), node.getTreeLevel())
        )).orderBy(cb.asc(root.<Long>get(LEFT)));
        try {
            return Optional.of(entityManager.createQuery(select).setMaxResults(1).getSingleResult());
        } catch (NoResultException ex) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<N> getNextSibling(N node, Class<N> nodeClass) {
      CriteriaBuilder cb = entityManager.getCriteriaBuilder();
      CriteriaQuery<N> select = cb.createQuery(nodeClass);
      Root<N> root = select.from(nodeClass);
      select.where(getPredicates(cb, root,
          cb.equal(root.<Long>get(LEFT), node.getTreeRight() + 1),
          cb.equal(root.<Long>get(LEVEL), node.getTreeLevel())
      )).orderBy(cb.asc(root.<Long>get(LEFT)));
      try {
          return Optional.of(entityManager.createQuery(select).setMaxResults(1).getSingleResult());
      } catch (NoResultException ex) {
          return Optional.empty();
      }
    }

    @Override
    public Optional<NestedNodeInfo<ID, N>> getNodeInfo(ID nodeId, Class<N> nodeClass, Class<ID> idClass) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<NestedNodeInfo> select = cb.createQuery(NestedNodeInfo.class);
        Root<N> root = select.from(nodeClass);
        select.select(
                cb.construct(
                        NestedNodeInfo.class,
                        root.get(ID),
                        root.get(PARENT_ID),
                        root.get(LEFT),
                        root.get(RIGHT),
                        root.get(LEVEL)
                )
        ).where(cb.equal(root.get(ID), nodeId));
        try {
            NestedNodeInfo<ID, N> result = entityManager.createQuery(select).getSingleResult();
            result.setNodeClass(nodeClass);
            result.setIdClass(idClass);
            return Optional.of(result);
        } catch (NoResultException ex) {
            return Optional.empty();
        }
    }

}
