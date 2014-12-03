/* 
 * The MIT License
 *
 * Copyright 2014 exsio.
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
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package pl.exsio.nestedj.retriever;

import java.util.ArrayList;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import pl.exsio.nestedj.NestedNodeUtil;
import pl.exsio.nestedj.NestedNodeRetriever;
import pl.exsio.nestedj.config.NestedNodeConfig;
import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.model.TreeImpl;
import pl.exsio.nestedj.model.Tree;

/**
 *
 * @author exsio
 * @param <T>
 */
public class NestedNodeRetrieverImpl<T extends NestedNode> implements NestedNodeRetriever<T> {

    @PersistenceContext
    protected EntityManager em;

    protected NestedNodeUtil<T> util;

    public NestedNodeRetrieverImpl() {
    }

    public NestedNodeRetrieverImpl(EntityManager em) {
        this.em = em;
    }

    public void setNestedNodeUtil(NestedNodeUtil util) {
        this.util = util;
    }

    @Override
    public Tree<T> getTree(T node) {
        Tree<T> tree = new TreeImpl<T>(node);
        for (T n : this.getChildren(node)) {
            Tree<T> subtree = this.getTree(n);
            tree.addChild(subtree);
        }
        return tree;
    }

    @Override
    public Iterable<T> getTreeAsList(T node) {

        NestedNodeConfig config = this.util.getNodeConfig(node.getClass());
        if (node instanceof NestedNode) {
            return this.em.createQuery("from " + config.getEntityName() +" "
                    + "where " + config.getLeftFieldName() + ">=:lft "
                    + "and " + config.getRightFieldName() + " <=:rgt "
                    + "order by " + config.getLeftFieldName() + " asc")
                    .setParameter("lft", node.getLeft())
                    .setParameter("rgt", node.getRight())
                    .getResultList();
        } else {
            return this.em.createQuery("from " + config.getEntityName() + " "
                    + "order by " + config.getLeftFieldName() + " asc")
                    .getResultList();
        }
    }

    @Override
    public Iterable<T> getChildren(T node) {
        NestedNodeConfig config = this.util.getNodeConfig(node.getClass());
        return this.em.createQuery("from " + config.getEntityName() + " "
                + "where " + config.getLeftFieldName() + ">=:lft "
                + "and " + config.getRightFieldName() + " <=:rgt "
                + "and " + config.getLevelFieldName() + " = :lvl "
                + "order by " + config.getLeftFieldName() + " asc")
                .setParameter("lft", node.getLeft())
                .setParameter("rgt", node.getRight())
                .setParameter("lvl", node.getLevel() + 1)
                .getResultList();
    }

    @Override
    public T getParent(T node) {
        if (node.getLevel() > 0) {
            NestedNodeConfig config = this.util.getNodeConfig(node.getClass());
            return (T) this.em.createQuery("from " + config.getEntityName() + " "
                    + "where " + config.getLeftFieldName() + "<:lft "
                    + "and " + config.getRightFieldName() + ">:rgt "
                    + "and " + config.getLevelFieldName() + " = :lvl")
                    .setParameter("lft", node.getLeft())
                    .setParameter("rgt", node.getRight())
                    .setParameter("lvl", node.getLevel() - 1)
                    .getSingleResult();
        } else {
            return null;
        }
    }

    @Override
    public Iterable<T> getParents(T node) {
        if (node.getLevel() > 0) {
            NestedNodeConfig config = this.util.getNodeConfig(node.getClass());
            return this.em.createQuery("from " + config.getEntityName() + " "
                    + "where " + config.getLeftFieldName() + "<:lft "
                    + "and " + config.getRightFieldName() + ">:rgt "
                    + "order by " + config.getLevelFieldName() + " desc")
                    .setParameter("lft", node.getLeft())
                    .setParameter("rgt", node.getRight())
                    .getResultList();
        } else {
            return new ArrayList();
        }
    }

}
