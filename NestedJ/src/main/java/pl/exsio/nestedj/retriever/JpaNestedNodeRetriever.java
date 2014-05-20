/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.exsio.nestedj.retriever;

import java.util.ArrayList;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import pl.exsio.nestedj.NestedNodeUtil;
import pl.exsio.nestedj.NestedNodeRetriever;
import pl.exsio.nestedj.config.NestedNodeConfig;
import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.model.NestedTree;
import pl.exsio.nestedj.model.Tree;

/**
 *
 * @author exsio
 * @param <T>
 */
public class JpaNestedNodeRetriever<T extends NestedNode> implements NestedNodeRetriever<T> {

    /**
     *
     */
    @PersistenceContext
    protected EntityManager em;

    /**
     *
     */
    protected NestedNodeUtil<T> util;

    /**
     * 
     */
    public JpaNestedNodeRetriever() {
    }

    /**
     * 
     * @param em 
     */
    public JpaNestedNodeRetriever(EntityManager em) {
        this.em = em;
    }

    
    /**
     *
     * @param util
     */
    public void setNestedNodeUtil(NestedNodeUtil util) {
        this.util = util;
    }

    /**
     *
     * @param node
     * @return
     */
    @Override
    public Tree<T> getTree(T node) {
        Tree<T> tree = new NestedTree<T>(node);
        for (T n : this.getChildren(node)) {
            Tree<T> subtree = this.getTree(n);
            tree.addChild(subtree);
        }
        return tree;
    }

    /**
     *
     * @param node
     * @return
     */
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

    /**
     *
     * @param node
     * @return
     */
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

    /**
     *
     * @param node
     * @return
     */
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

    /**
     * 
     * @param node
     * @return 
     */
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
