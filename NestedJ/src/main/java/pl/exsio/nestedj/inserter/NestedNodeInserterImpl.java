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
package pl.exsio.nestedj.inserter;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.NestedNodeInserter;
import pl.exsio.nestedj.config.NestedNodeConfig;
import javax.transaction.Transactional;
import pl.exsio.nestedj.util.NestedNodeUtil;

/**
 *
 * @author exsio
 * @param <T>
 */
public class NestedNodeInserterImpl<T extends NestedNode> implements NestedNodeInserter<T> {

    @PersistenceContext
    protected EntityManager em;

    public NestedNodeInserterImpl() {
    }

    public NestedNodeInserterImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    @Transactional
    public T insert(T node, T parent, int mode) {
        this.em.refresh(parent);
        NestedNodeConfig config = NestedNodeUtil.getNodeConfig(node.getClass());
        this.makeSpaceForNewElement(parent.getRight(), this.isGte(mode), config);
        this.insertNodeIntoTable(node);
        this.insertNodeIntoTree(config, parent, node, mode);
        this.em.refresh(node);
        return node;
    }

    private void insertNodeIntoTable(T node) {
        this.em.persist(node);
        this.em.flush();
    }

    private void insertNodeIntoTree(NestedNodeConfig config, T parent, T node, int mode) {
        Long left = this.getNodeLeft(parent, mode);
        Long right = left + 1;
        Long level = this.getNodeLevel(parent, mode);
        NestedNode nodeParent = this.getNodeParent(parent, mode);
        this.em.createQuery(
                "update " + config.getEntityName() + " "
                + "set " + config.getParentFieldName() + " = :parent,"
                + config.getLeftFieldName() + " = :left,"
                + config.getRightFieldName() + " = :right,"
                + config.getLevelFieldName() + " = :level "
                + "where id = :id").setParameter("parent", nodeParent)
                .setParameter("left", left)
                .setParameter("right", right)
                .setParameter("level", level)
                .setParameter("id", node.getId())
                .executeUpdate();
    }

    protected Long getNodeLevel(NestedNode parent, int mode) {
        switch (mode) {
            case MODE_NEXT_SIBLING:
            case MODE_PREV_SIBLING:
                return parent.getLevel();
            case MODE_LAST_CHILD:
            case MODE_FIRST_CHILD:
            default:
                return parent.getLevel() + 1;
        }
    }

    protected NestedNode getNodeParent(NestedNode parent, int mode) {
        switch (mode) {
            case MODE_NEXT_SIBLING:
            case MODE_PREV_SIBLING:
                return parent.getParent();
            case MODE_LAST_CHILD:
            case MODE_FIRST_CHILD:
            default:
                return parent;
        }
    }

    protected boolean isGte(int mode) {
        switch (mode) {
            case MODE_NEXT_SIBLING:
            case MODE_FIRST_CHILD:
                return false;
            case MODE_PREV_SIBLING:
            case MODE_LAST_CHILD:
            default:
                return true;
        }
    }

    protected Long getNodeLeft(NestedNode parent, int mode) {
        switch (mode) {
            case MODE_NEXT_SIBLING:
                return parent.getRight() + 1;
            case MODE_PREV_SIBLING:
                return parent.getLeft();
            case MODE_FIRST_CHILD:
                return parent.getLeft() + 1;
            case MODE_LAST_CHILD:
            default:
                return parent.getRight();
        }
    }

    protected void makeSpaceForNewElement(Long from, boolean gte, NestedNodeConfig config) {

        String sign = gte ? " >= " : " > ";
        this.updateLeftFields(config, sign, from);
        this.updateRightFields(config, sign, from);
    }

    private void updateRightFields(NestedNodeConfig config, String sign, Long from) {
        String rightQuery = "update " + config.getEntityName() + " "
                + "set " + config.getRightFieldName() + " = " + config.getRightFieldName() + "+2 "
                + "where " + config.getRightFieldName() + " " + sign + " :from";
        this.em.createQuery(rightQuery)
                .setParameter("from", from)
                .executeUpdate();
    }

    private void updateLeftFields(NestedNodeConfig config, String sign, Long from) {
        String leftQuery = "update " + config.getEntityName() + " "
                + "set " + config.getLeftFieldName() + " = " + config.getLeftFieldName() + "+2 "
                + "where " + config.getLeftFieldName() + " " + sign + " :from";
        this.em.createQuery(leftQuery)
                .setParameter("from", from)
                .executeUpdate();
    }

}
