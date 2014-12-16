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
import static pl.exsio.nestedj.util.NestedNodeUtil.*;

/**
 *
 * @author exsio
 */
public class NestedNodeRemoverImpl implements NestedNodeRemover {

    @PersistenceContext
    protected EntityManager em;

    protected Class<? extends NestedNode> c;

    public NestedNodeRemoverImpl() {
    }

    public NestedNodeRemoverImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    @Transactional
    public void removeSingle(NestedNode node) {
        this.c = node.getClass();

        Long from = node.getRight();
        NestedNode parent = null;
        parent = this.findNodeParent(node, parent);
        this.updateNodesParent(node, parent);
        this.prepareTreeForSingleNodeRemoval(from);
        this.updateDeletedNodeChildren(node);
        this.em.remove(node);
        this.em.flush();
        this.em.clear();

    }

    protected void prepareTreeForSingleNodeRemoval(Long from) {
        this.updateLeftFieldsBeforeSingleNodeRemoval(from);
        this.updateRightFieldsBeforeSingleNodeRemoval(from);
    }

    protected void updateDeletedNodeChildren(NestedNode node) {
        this.em.createQuery("update " + entity(c) + " "
                + "set " + right(c) + " = " + right(c) + "-1, "
                + left(c) + " = " + left(c) + "-1, "
                + level(c) + " = " + level(c) + "-1 "
                + "where " + left(c) + " > :lft "
                + "and " + right(c) + " < :rgt")
                .setParameter("lft", node.getLeft())
                .setParameter("rgt", node.getRight())
                .executeUpdate();
    }

    protected void updateRightFieldsBeforeSingleNodeRemoval(Long from) {
        String rightQuery = "update " + entity(c) + " "
                + "set " + right(c) + " = " + right(c) + "-2 "
                + "where " + right(c) + " > :from";
        this.em.createQuery(rightQuery).setParameter("from", from).executeUpdate();
    }

    protected void updateLeftFieldsBeforeSingleNodeRemoval(Long from) {
        String leftQuery = "update " + entity(c) + " "
                + "set " + left(c) + " = " + left(c) + "-2 "
                + "where " + left(c) + " > :from";
        this.em.createQuery(leftQuery).setParameter("from", from).executeUpdate();
    }

    protected void updateNodesParent(NestedNode node, NestedNode parent) {
        this.em.createQuery("update " + entity(c) + " "
                + "set parent = :parent "
                + "where " + left(c) + ">=:lft "
                + "and " + right(c) + " <=:rgt "
                + "and " + level(c) + " = :lvl")
                .setParameter("lft", node.getLeft())
                .setParameter("rgt", node.getRight())
                .setParameter("lvl", node.getLevel() + 1)
                .setParameter("parent", parent)
                .executeUpdate();
    }

    protected NestedNode findNodeParent(NestedNode node, NestedNode parent) {
        if (node.getLevel() > 0) {
            parent = (NestedNode) this.em.createQuery("from " + entity(c) + " "
                    + "where " + left(c) + "<:lft "
                    + "and " + right(c) + ">:rgt "
                    + "and " + level(c) + " = :lvl")
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

        this.c = node.getClass();
        Long delta = node.getRight() - node.getLeft() + 1;
        Long from = node.getRight();
        this.performBatchDeletion(node);
        this.updateLeftFieldsAfterSubtreeRemoval(from, delta);
        this.updateRightFieldsAfterSubtreeRemoval(from, delta);
        this.em.clear();

    }

    protected void updateRightFieldsAfterSubtreeRemoval(Long from, Long delta) {
        String rightQuery = "update " + entity(c) + " "
                + "set " + right(c) + " = " + right(c) + "-:delta "
                + "where " + right(c) + " > :from";
        this.em.createQuery(rightQuery).setParameter("from", from).setParameter("delta", delta).executeUpdate();
    }

    protected void updateLeftFieldsAfterSubtreeRemoval(Long from, Long delta) {
        String leftQuery = "update " + entity(c) + " "
                + "set " + left(c) + " = " + left(c) + "-:delta "
                + "where " + left(c) + " > :from";
        this.em.createQuery(leftQuery).setParameter("from", from).setParameter("delta", delta).executeUpdate();
    }

    protected void performBatchDeletion(NestedNode node) {
        this.em.createQuery("delete from " + entity(c) + " "
                + "where " + left(c) + " >= :lft "
                + "and " + right(c) + " <= :rgt")
                .setParameter("lft", node.getLeft())
                .setParameter("rgt", node.getRight())
                .executeUpdate();
    }

    public void setEntityManager(EntityManager em) {
        this.em = em;
    }

}
