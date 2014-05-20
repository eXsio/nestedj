/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.exsio.nestedj.remover;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.NestedNodeUtil;
import pl.exsio.nestedj.NestedNodeRemover;
import pl.exsio.nestedj.config.NestedNodeConfig;

/**
 *
 * @author exsio
 */
public class JpaNestedNodeRemover implements NestedNodeRemover {

    @PersistenceContext
    protected EntityManager em;

    /**
     *
     */
    protected NestedNodeUtil util;

    /**
     *
     */
    public JpaNestedNodeRemover() {
    }

    /**
     *
     * @param em
     */
    public JpaNestedNodeRemover(EntityManager em) {
        this.em = em;
    }

    /**
     *
     * @param util
     */
    public void setNestedNodeUtil(NestedNodeUtil util) {
        this.util = util;
    }

    @Override
    @Transactional
    public void removeSingle(NestedNode node) {

        NestedNodeConfig config = this.util.getNodeConfig(node.getClass());
        Long from = node.getRight();
        NestedNode parent = null;
        parent = this.findNodeParent(node, parent, config);
        this.updateNodesParent(config, node, parent);
        this.prepareTreeForSingleNodeRemoval(config, from);
        this.updateDeletedNodeChildren(config, node);
        this.em.remove(node);
        this.em.flush();
        this.em.clear();

    }

    private void prepareTreeForSingleNodeRemoval(NestedNodeConfig config, Long from) {
        this.updateLeftFieldsBeforeSingleNodeRemoval(config, from);
        this.updateRightFieldsBeforeSingleNodeRemoval(config, from);
    }

    /**
     * 
     * @param config
     * @param node 
     */
    private void updateDeletedNodeChildren(NestedNodeConfig config, NestedNode node) {
        this.em.createQuery("update " + config.getEntityName()+ " "
                + "set " + config.getRightFieldName() + " = " + config.getRightFieldName() + "-1, " 
                + config.getLeftFieldName() + " = " + config.getLeftFieldName() + "-1 "
                + "where " + config.getLeftFieldName() + " > :lft "
                + "and " + config.getRightFieldName() + " < :rgt")
                .setParameter("lft", node.getLeft())
                .setParameter("rgt", node.getRight())
                .executeUpdate();
    }

    /**
     * 
     * @param config
     * @param from 
     */
    private void updateRightFieldsBeforeSingleNodeRemoval(NestedNodeConfig config, Long from) {
        String rightQuery = "update " + config.getEntityName()+ " "
                + "set " + config.getRightFieldName() + " = " + config.getRightFieldName() + "-2 "
                + "where " + config.getRightFieldName() + " > :from";
        this.em.createQuery(rightQuery).setParameter("from", from).executeUpdate();
    }

    /**
     * 
     * @param config
     * @param from 
     */
    private void updateLeftFieldsBeforeSingleNodeRemoval(NestedNodeConfig config, Long from) {
        String leftQuery = "update " + config.getEntityName()+ " "
                + "set " + config.getLeftFieldName() + " = " + config.getLeftFieldName() + "-2 "
                + "where " + config.getLeftFieldName() + " > :from";
        this.em.createQuery(leftQuery).setParameter("from", from).executeUpdate();
    }

    /**
     * 
     * @param config
     * @param node
     * @param parent 
     */
    private void updateNodesParent(NestedNodeConfig config, NestedNode node, NestedNode parent) {
        this.em.createQuery("update " + config.getEntityName()+ " "
                + "set parent = :parent "
                + "where " + config.getLeftFieldName() + ">=:lft "
                + "and " + config.getRightFieldName() + " <=:rgt "
                + "and " + config.getLevelFieldName() + " = :lvl")
                .setParameter("lft", node.getLeft())
                .setParameter("rgt", node.getRight())
                .setParameter("lvl", node.getLevel() + 1)
                .setParameter("parent", parent)
                .executeUpdate();
    }

    /**
     * 
     * @param node
     * @param parent
     * @param config
     * @return 
     */
    private NestedNode findNodeParent(NestedNode node, NestedNode parent, NestedNodeConfig config) {
        if (node.getLevel() > 0) {
            parent = (NestedNode) this.em.createQuery("from " + config.getEntityName() + " "
                    + "where " + config.getLeftFieldName() + "<:lft "
                    + "and " + config.getRightFieldName() + ">:rgt "
                    + "and " + config.getLevelFieldName() + " = :lvl")
                    .setParameter("lft", node.getLeft())
                    .setParameter("rgt", node.getRight())
                    .setParameter("lvl", node.getLevel() - 1)
                    .getSingleResult();
        }
        return parent;
    }

    /**
     * 
     * @param node 
     */
    @Override
    @Transactional
    public void removeSubtree(NestedNode node) {

        NestedNodeConfig config = this.util.getNodeConfig(node.getClass());
        Long delta = node.getRight() - node.getLeft() + 1;
        Long from = node.getRight();
        this.performBatchDeletion(config, node);
        this.updateLeftFieldsAfterSubtreeRemoval(config, from, delta);
        this.updateRightFieldsAfterSubtreeRemoval(config, from, delta);
        this.em.clear();

    }

    /**
     * 
     * @param config
     * @param from
     * @param delta 
     */
    private void updateRightFieldsAfterSubtreeRemoval(NestedNodeConfig config, Long from, Long delta) {
        String rightQuery = "update " + config.getEntityName()+ " "
                + "set " + config.getRightFieldName() + " = " + config.getRightFieldName() + "-:delta "
                + "where " + config.getRightFieldName() + " > :from";
        this.em.createQuery(rightQuery).setParameter("from", from).setParameter("delta", delta).executeUpdate();
    }

    /**
     * 
     * @param config
     * @param from
     * @param delta 
     */
    private void updateLeftFieldsAfterSubtreeRemoval(NestedNodeConfig config, Long from, Long delta) {
        String leftQuery = "update " + config.getEntityName()+ " "
                + "set " + config.getLeftFieldName() + " = " + config.getLeftFieldName() + "-:delta "
                + "where " + config.getLeftFieldName() + " > :from";
        this.em.createQuery(leftQuery).setParameter("from", from).setParameter("delta", delta).executeUpdate();
    }

    /**
     * 
     * @param config
     * @param node 
     */
    private void performBatchDeletion(NestedNodeConfig config, NestedNode node) {
        this.em.createQuery("delete from " + config.getEntityName()+ " "
                + "where " + config.getLeftFieldName() + " >= :lft "
                + "and " + config.getRightFieldName() + " <= :rgt")
                .setParameter("lft", node.getLeft())
                .setParameter("rgt", node.getRight())
                .executeUpdate();
    }

}
