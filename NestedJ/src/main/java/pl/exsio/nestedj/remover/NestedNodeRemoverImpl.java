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
package pl.exsio.nestedj.remover;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.NestedNodeRemover;
import pl.exsio.nestedj.config.NestedNodeConfig;
import pl.exsio.nestedj.util.NestedNodeUtil;

/**
 *
 * @author exsio
 */
public class NestedNodeRemoverImpl implements NestedNodeRemover {

    @PersistenceContext
    protected EntityManager em;

    public NestedNodeRemoverImpl() {
    }

    public NestedNodeRemoverImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    @Transactional
    public void removeSingle(NestedNode node) {

        NestedNodeConfig config = NestedNodeUtil.getNodeConfig(node.getClass());
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

    private void updateDeletedNodeChildren(NestedNodeConfig config, NestedNode node) {
        this.em.createQuery("update " + config.getEntityName() + " "
                + "set " + config.getRightFieldName() + " = " + config.getRightFieldName() + "-1, "
                + config.getLeftFieldName() + " = " + config.getLeftFieldName() + "-1, "
                + config.getLevelFieldName() + " = " + config.getLevelFieldName() + "-1 "
                + "where " + config.getLeftFieldName() + " > :lft "
                + "and " + config.getRightFieldName() + " < :rgt")
                .setParameter("lft", node.getLeft())
                .setParameter("rgt", node.getRight())
                .executeUpdate();
    }

    private void updateRightFieldsBeforeSingleNodeRemoval(NestedNodeConfig config, Long from) {
        String rightQuery = "update " + config.getEntityName() + " "
                + "set " + config.getRightFieldName() + " = " + config.getRightFieldName() + "-2 "
                + "where " + config.getRightFieldName() + " > :from";
        this.em.createQuery(rightQuery).setParameter("from", from).executeUpdate();
    }

    private void updateLeftFieldsBeforeSingleNodeRemoval(NestedNodeConfig config, Long from) {
        String leftQuery = "update " + config.getEntityName() + " "
                + "set " + config.getLeftFieldName() + " = " + config.getLeftFieldName() + "-2 "
                + "where " + config.getLeftFieldName() + " > :from";
        this.em.createQuery(leftQuery).setParameter("from", from).executeUpdate();
    }

    private void updateNodesParent(NestedNodeConfig config, NestedNode node, NestedNode parent) {
        this.em.createQuery("update " + config.getEntityName() + " "
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

    @Override
    @Transactional
    public void removeSubtree(NestedNode node) {

        NestedNodeConfig config = NestedNodeUtil.getNodeConfig(node.getClass());
        Long delta = node.getRight() - node.getLeft() + 1;
        Long from = node.getRight();
        this.performBatchDeletion(config, node);
        this.updateLeftFieldsAfterSubtreeRemoval(config, from, delta);
        this.updateRightFieldsAfterSubtreeRemoval(config, from, delta);
        this.em.clear();

    }

    private void updateRightFieldsAfterSubtreeRemoval(NestedNodeConfig config, Long from, Long delta) {
        String rightQuery = "update " + config.getEntityName() + " "
                + "set " + config.getRightFieldName() + " = " + config.getRightFieldName() + "-:delta "
                + "where " + config.getRightFieldName() + " > :from";
        this.em.createQuery(rightQuery).setParameter("from", from).setParameter("delta", delta).executeUpdate();
    }

    private void updateLeftFieldsAfterSubtreeRemoval(NestedNodeConfig config, Long from, Long delta) {
        String leftQuery = "update " + config.getEntityName() + " "
                + "set " + config.getLeftFieldName() + " = " + config.getLeftFieldName() + "-:delta "
                + "where " + config.getLeftFieldName() + " > :from";
        this.em.createQuery(leftQuery).setParameter("from", from).setParameter("delta", delta).executeUpdate();
    }

    private void performBatchDeletion(NestedNodeConfig config, NestedNode node) {
        this.em.createQuery("delete from " + config.getEntityName() + " "
                + "where " + config.getLeftFieldName() + " >= :lft "
                + "and " + config.getRightFieldName() + " <= :rgt")
                .setParameter("lft", node.getLeft())
                .setParameter("rgt", node.getRight())
                .executeUpdate();
    }

    public void setEntityManager(EntityManager em) {
        this.em = em;
    }

}
