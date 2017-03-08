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
package pl.exsio.nestedj.delegate;

import pl.exsio.nestedj.discriminator.TreeDiscriminator;
import pl.exsio.nestedj.ex.InvalidNodesHierarchyException;
import pl.exsio.nestedj.model.NestedNode;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;
import java.util.List;

import static pl.exsio.nestedj.util.NestedNodeUtil.id;
import static pl.exsio.nestedj.util.NestedNodeUtil.left;
import static pl.exsio.nestedj.util.NestedNodeUtil.level;
import static pl.exsio.nestedj.util.NestedNodeUtil.parent;
import static pl.exsio.nestedj.util.NestedNodeUtil.right;

/**
 * @author exsio
 */
public class NestedNodeMoverImpl<N extends NestedNode<N>> extends NestedNodeDelegate<N> implements NestedNodeMover<N> {

    private enum Sign {
        PLUS, MINUS
    }

    @PersistenceContext
    private EntityManager em;

    public NestedNodeMoverImpl(TreeDiscriminator<N> treeDiscriminator) {
        super(treeDiscriminator);
    }

    public NestedNodeMoverImpl(EntityManager em, TreeDiscriminator<N> treeDiscriminator) {
        super(treeDiscriminator);
        this.em = em;
    }

    @Override
    public N move(N node, N parent, Mode mode) throws InvalidNodesHierarchyException {
        Class<N> nodeClass = getNodeClass(node);
        if (!this.canMoveNodeToSelectedParent(node, parent)) {
            throw new InvalidNodesHierarchyException("You cannot move a parent node to it's child or move a node to itself");
        }
        Sign sign = this.getSign(node, parent, mode);
        Long start = this.getStart(node, parent, mode, sign);
        Long stop = this.getStop(node, parent, mode, sign);
        List<Long> nodeIds = this.getNodeIds(node, nodeClass);
        Long delta = this.getDelta(nodeIds);
        Long nodeDelta = this.getNodeDelta(start, stop);
        Sign nodeSign = this.getNodeSign(sign);
        Long levelModificator = this.getLevelModificator(node, parent, mode);
        N newParent = this.getNewParent(parent, mode);

        this.makeSpaceForMovedElement(sign, delta, start, stop, nodeClass);
        this.performMove(nodeSign, nodeDelta, nodeIds, levelModificator, nodeClass);
        this.updateParentField(newParent, node, nodeClass);

        return node;
    }

    private void makeSpaceForMovedElement(Sign sign, Long delta, Long start, Long stop, Class<N> nodeClass) {
        this.updateFields(sign, delta, start, stop, nodeClass, right(nodeClass));
        this.updateFields(sign, delta, start, stop, nodeClass, left(nodeClass));
    }

    private void updateParentField(NestedNode newParent, NestedNode node, Class<N> nodeClass) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaUpdate<N> update = cb.createCriteriaUpdate(nodeClass);
        Root<N> root = update.from(nodeClass);

        update.set(root.get(parent(nodeClass)), newParent)
                .where(getPredicates(cb, root, cb.equal(root.get(id(nodeClass)), node.getId())));

        em.createQuery(update).executeUpdate();
    }

    private void performMove(Sign nodeSign, Long nodeDelta, List nodeIds, Long levelModificator, Class<N> nodeClass) {
        if (!nodeIds.isEmpty()) {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaUpdate<N> update = cb.createCriteriaUpdate(nodeClass);
            Root<N> root = update.from(nodeClass);

            update.set(root.<Long>get(level(nodeClass)), cb.sum(root.<Long>get(level(nodeClass)), levelModificator));
            if (Sign.MINUS.equals(nodeSign)) {
                update.set(root.<Long>get(right(nodeClass)), cb.diff(root.<Long>get(right(nodeClass)), nodeDelta));
                update.set(root.<Long>get(left(nodeClass)), cb.diff(root.<Long>get(left(nodeClass)), nodeDelta));
            } else if (Sign.PLUS.equals(nodeSign)) {
                update.set(root.<Long>get(right(nodeClass)), cb.sum(root.<Long>get(right(nodeClass)), nodeDelta));
                update.set(root.<Long>get(left(nodeClass)), cb.sum(root.<Long>get(left(nodeClass)), nodeDelta));
            }
            update.where(getPredicates(cb, root, root.get(id(nodeClass)).in(nodeIds)));

            em.createQuery(update).executeUpdate();
        }
    }

    private void updateFields(Sign sign, Long delta, Long start, Long stop, Class<N> nodeClass, String field) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaUpdate<N> update = cb.createCriteriaUpdate(nodeClass);
        Root<N> root = update.from(nodeClass);

        if (Sign.MINUS.equals(sign)) {
            update.set(root.<Long>get(field), cb.diff(root.<Long>get(field), delta));
        } else if (Sign.PLUS.equals(sign)) {
            update.set(root.<Long>get(field), cb.sum(root.<Long>get(field), delta));
        }
        update.where(getPredicates(cb, root,
                cb.greaterThan(root.<Long>get(field), start),
                cb.lessThan(root.<Long>get(field), stop)
        ));

        em.createQuery(update).executeUpdate();
    }

    private boolean canMoveNodeToSelectedParent(N node, NestedNode parent) {
        return !node.getId().equals(parent.getId()) && (node.getLeft() >= parent.getLeft() || node.getRight() <= parent.getRight());
    }
    
    private List<Long> getNodeIds(N node, Class<N> nodeClass) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> select = cb.createQuery(Long.class);
        Root<N> root = select.from(nodeClass);
        select.select(root.<Long>get(id(nodeClass))).where(
                getPredicates(cb, root,
                        cb.greaterThanOrEqualTo(root.<Long>get(left(nodeClass)), node.getLeft()),
                        cb.lessThanOrEqualTo(root.<Long>get(right(nodeClass)), node.getRight())
                ));
        return em.createQuery(select).getResultList();
    }

    private N getNewParent(N parent, Mode mode) {
        switch (mode) {
            case NEXT_SIBLING:
            case PREV_SIBLING:
                return (N) parent.getParent();
            case FIRST_CHILD:
            case LAST_CHILD:
            default:
                return parent;
        }
    }

    private Long getLevelModificator(N node, N parent, Mode mode) {
        switch (mode) {
            case NEXT_SIBLING:
            case PREV_SIBLING:
                return parent.getLevel() - node.getLevel();
            case FIRST_CHILD:
            case LAST_CHILD:
            default:
                return parent.getLevel() + 1 - node.getLevel();
        }
    }

    private Long getNodeDelta(Long start, Long stop) {
        return stop - start - 1;
    }

    private Long getDelta(List<Long> nodeIds) {
        return (long) nodeIds.size() * 2;
    }

    private Sign getNodeSign(Sign sign) {
        return (sign.equals(Sign.PLUS)) ? Sign.MINUS : Sign.PLUS;
    }

    private Sign getSign(N node, N parent, Mode mode) {
        switch (mode) {
            case PREV_SIBLING:
            case FIRST_CHILD:
                return (node.getRight() - parent.getLeft()) > 0 ? Sign.PLUS : Sign.MINUS;
            case NEXT_SIBLING:
            case LAST_CHILD:
            default:
                return (node.getLeft() - parent.getRight()) > 0 ? Sign.PLUS : Sign.MINUS;
        }
    }

    private Long getStart(N node, N parent, Mode mode, Sign sign) {
        switch (mode) {
            case PREV_SIBLING:
                return sign.equals(Sign.PLUS) ? parent.getLeft() - 1 : node.getRight();
            case FIRST_CHILD:
                return sign.equals(Sign.PLUS) ? parent.getLeft() : node.getRight();
            case NEXT_SIBLING:
                return sign.equals(Sign.PLUS) ? parent.getRight() : node.getRight();
            case LAST_CHILD:
            default:
                return sign.equals(Sign.PLUS) ? parent.getRight() - 1 : node.getRight();

        }
    }

    private Long getStop(N node, N parent, Mode mode, Sign sign) {
        switch (mode) {
            case PREV_SIBLING:
                return sign.equals(Sign.PLUS) ? node.getLeft() : parent.getLeft();
            case FIRST_CHILD:
                return sign.equals(Sign.PLUS) ? node.getLeft() : parent.getLeft() + 1;
            case NEXT_SIBLING:
                return sign.equals(Sign.PLUS) ? node.getLeft() : parent.getRight() + 1;
            case LAST_CHILD:
            default:
                return sign.equals(Sign.PLUS) ? node.getLeft() : parent.getRight();
        }
    }

    public void setEntityManager(EntityManager em) {
        this.em = em;
    }

}
