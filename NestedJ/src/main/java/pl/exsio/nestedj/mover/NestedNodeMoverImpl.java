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
package pl.exsio.nestedj.mover;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.NestedNodeMover;
import pl.exsio.nestedj.ex.InvalidNodesHierarchyException;
import static pl.exsio.nestedj.util.NestedNodeUtil.*;

/**
 *
 * @author exsio
 */
public class NestedNodeMoverImpl implements NestedNodeMover {

    private final static String SIGN_PLUS = "+";
    private final static String SIGN_MINUS = "-";

    @PersistenceContext
    protected EntityManager em;

    protected Class<? extends NestedNode> c;

    public NestedNodeMoverImpl() {
    }

    public NestedNodeMoverImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    @Transactional
    public NestedNode move(NestedNode node, NestedNode parent, int mode) throws InvalidNodesHierarchyException {

        this.c = node.getClass();
        this.em.refresh(node);
        this.em.refresh(parent);
        if (!this.canMoveNodeToSelectedParent(node, parent)) {
            throw new InvalidNodesHierarchyException("You cannot move a parent node to it's child or move a node to itself");
        }
        String sign = this.getSign(node, parent, mode);
        Long start = this.getStart(node, parent, mode, sign);
        Long stop = this.getStop(node, parent, mode, sign);
        List nodeIds = this.getNodeIds(node);
        Long delta = this.getDelta(nodeIds);
        Long nodeDelta = this.getNodeDelta(start, stop);
        String nodeSign = this.getNodeSign(sign);
        Long levelModificator = this.getLevelModificator(node, parent, mode);
        NestedNode newParent = this.getNewParent(parent, mode);

        this.makeSpaceForMovedElement(sign, delta, start, stop);
        this.performMove(nodeSign, nodeDelta, nodeIds, levelModificator);
        this.updateParentField(newParent, node);

        this.em.refresh(parent);
        this.em.refresh(node);

        return node;
    }

    protected void makeSpaceForMovedElement(String sign, Long delta, Long start, Long stop) {
        this.updateLeftFields(sign, delta, start, stop);
        this.updateRightFields(sign, delta, start, stop);
    }

    protected void updateParentField(NestedNode newParent, NestedNode node) {
        this.em.createQuery("update " + entity(c) + " "
                + "set " + parent(c) + " = :parent "
                + "where " + id(c) + " = :id").setParameter("parent", newParent)
                .setParameter("id", node.getId())
                .executeUpdate();
    }

    protected void performMove(String nodeSign, Long nodeDelta, List nodeIds, Long levelModificator) {
        if (!nodeIds.isEmpty()) {
            this.em.createQuery("update " + entity(c) + " "
                    + "set " + level(c) + " = " + level(c) + " + :levelModificator, "
                    + right(c) + " = " + right(c) + " " + nodeSign + ":nodeDelta, "
                    + left(c) + " = " + left(c) + " " + nodeSign + ":nodeDelta "
                    + "where " + id(c) + " in :ids")
                    .setParameter("nodeDelta", nodeDelta)
                    .setParameter("ids", nodeIds)
                    .setParameter("levelModificator", levelModificator)
                    .executeUpdate();
        }
    }

    protected void updateRightFields(String sign, Long delta, Long start, Long stop) {
        this.em.createQuery("update " + entity(c) + " "
                + "set " + right(c) + " = " + right(c) + " " + sign + ":delta "
                + "where " + right(c) + " > :start "
                + "and " + right(c) + " < :stop")
                .setParameter("delta", delta)
                .setParameter("start", start)
                .setParameter("stop", stop)
                .executeUpdate();
    }

    protected void updateLeftFields(String sign, Long delta, Long start, Long stop) {
        this.em.createQuery("update " + entity(c) + " "
                + "set " + left(c) + " = " + left(c) + " " + sign + ":delta where "
                + left(c) + " > :start "
                + "and " + left(c) + " < :stop")
                .setParameter("delta", delta)
                .setParameter("start", start)
                .setParameter("stop", stop)
                .executeUpdate();
    }

    protected boolean canMoveNodeToSelectedParent(NestedNode node, NestedNode parent) {
        return !node.getId().equals(parent.getId()) && (node.getLeft() >= parent.getLeft() || node.getRight() <= parent.getRight());
    }

    protected NestedNode getNewParent(NestedNode parent, int mode) {
        switch (mode) {
            case MODE_NEXT_SIBLING:
            case MODE_PREV_SIBLING:
                return parent.getParent();
            case MODE_FIRST_CHILD:
            case MODE_LAST_CHILD:
            default:
                return parent;
        }
    }

    protected Long getLevelModificator(NestedNode node, NestedNode parent, int mode) {
        switch (mode) {
            case MODE_NEXT_SIBLING:
            case MODE_PREV_SIBLING:
                return parent.getLevel() - node.getLevel();
            case MODE_FIRST_CHILD:
            case MODE_LAST_CHILD:
            default:
                return parent.getLevel() + 1 - node.getLevel();
        }
    }

    protected List<Long> getNodeIds(NestedNode node) {
        List result = this.em.createQuery("select id from " + entity(c) + " "
                + "where " + left(c) + ">=:lft "
                + "and " + right(c) + " <=:rgt ")
                .setParameter("lft", node.getLeft())
                .setParameter("rgt", node.getRight())
                .getResultList();
        return result;
    }

    protected Long getNodeDelta(Long start, Long stop) {
        return stop - start - 1;
    }

    protected Long getDelta(List<Long> nodeIds) {
        return (long) nodeIds.size() * 2;
    }

    protected String getNodeSign(String sign) {
        return (sign.equals(SIGN_PLUS)) ? SIGN_MINUS : SIGN_PLUS;
    }

    protected String getSign(NestedNode node, NestedNode parent, int mode) {
        switch (mode) {
            case MODE_PREV_SIBLING:
            case MODE_FIRST_CHILD:
                return (node.getRight() - parent.getLeft()) > 0 ? SIGN_PLUS : SIGN_MINUS;
            case MODE_NEXT_SIBLING:
            case MODE_LAST_CHILD:
            default:
                return (node.getLeft() - parent.getRight()) > 0 ? SIGN_PLUS : SIGN_MINUS;
        }
    }

    protected Long getStart(NestedNode node, NestedNode parent, int mode, String sign) {
        switch (mode) {
            case MODE_PREV_SIBLING:
                return sign.equals(SIGN_PLUS) ? parent.getLeft() - 1 : node.getRight();
            case MODE_FIRST_CHILD:
                return sign.equals(SIGN_PLUS) ? parent.getLeft() : node.getRight();
            case MODE_NEXT_SIBLING:
                return sign.equals(SIGN_PLUS) ? parent.getRight() : node.getRight();
            case MODE_LAST_CHILD:
            default:
                return sign.equals(SIGN_PLUS) ? parent.getRight() - 1 : node.getRight();

        }
    }

    protected Long getStop(NestedNode node, NestedNode parent, int mode, String sign) {
        switch (mode) {
            case MODE_PREV_SIBLING:
                return sign.equals(SIGN_PLUS) ? node.getLeft() : parent.getLeft();
            case MODE_FIRST_CHILD:
                return sign.equals(SIGN_PLUS) ? node.getLeft() : parent.getLeft() + 1;
            case MODE_NEXT_SIBLING:
                return sign.equals(SIGN_PLUS) ? node.getLeft() : parent.getRight() + 1;
            case MODE_LAST_CHILD:
            default:
                return sign.equals(SIGN_PLUS) ? node.getLeft() : parent.getRight();
        }
    }

    public void setEntityManager(EntityManager em) {
        this.em = em;
    }

}
