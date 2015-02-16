/* 
 * The MIT License
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
import javax.transaction.Transactional;
import static pl.exsio.nestedj.util.NestedNodeUtil.*;

/**
 *
 * @author exsio
 * @param <T>
 */
public class NestedNodeInserterImpl<T extends NestedNode> implements NestedNodeInserter<T> {

    @PersistenceContext
    protected EntityManager em;

    protected Class<? extends NestedNode> c;

    public NestedNodeInserterImpl() {
    }

    public NestedNodeInserterImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    @Transactional
    public T insert(T node, T parent, int mode) {
        this.c = node.getClass();
        this.em.refresh(parent);
        this.makeSpaceForNewElement(parent.getRight(), mode);
        this.insertNodeIntoTable(node);
        this.insertNodeIntoTree(parent, node, mode);
        this.em.refresh(node);
        return node;
    }

    protected void insertNodeIntoTable(T node) {
        this.em.persist(node);
        this.em.flush();
    }

    protected void insertNodeIntoTree(T parent, T node, int mode) {
        Long left = this.getNodeLeft(parent, mode);
        Long right = left + 1;
        Long level = this.getNodeLevel(parent, mode);
        NestedNode nodeParent = this.getNodeParent(parent, mode);
        this.em.createQuery(
                "update " + entity(c) + " "
                + "set " + parent(c) + " = :parent,"
                + left(c) + " = :left,"
                + right(c) + " = :right,"
                + level(c) + " = :level "
                + "where " + id(c) + " = :id")
                .setParameter("parent", nodeParent)
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

    protected void makeSpaceForNewElement(Long from, int mode) {

        String sign = this.isGte(mode) ? " >= " : " > ";
        this.updateLeftFields(sign, from);
        this.updateRightFields(sign, from);
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

    protected void updateRightFields(String sign, Long from) {
        this.em.createQuery("update " + entity(c) + " "
                + "set " + right(c) + " = " + right(c) + "+2 "
                + "where " + right(c) + " " + sign + " :from")
                .setParameter("from", from)
                .executeUpdate();
    }

    protected void updateLeftFields(String sign, Long from) {
        this.em.createQuery("update " + entity(c) + " "
                + "set " + left(c) + " = " + left(c) + "+2 "
                + "where " + left(c) + " " + sign + " :from")
                .setParameter("from", from)
                .executeUpdate();
    }

    public void setEntityManager(EntityManager em) {
        this.em = em;
    }

}
