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
package pl.exsio.nestedj.delegate.jpa;

import pl.exsio.nestedj.delegate.NestedNodeMover;
import pl.exsio.nestedj.discriminator.TreeDiscriminator;
import pl.exsio.nestedj.ex.InvalidNodesHierarchyException;
import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.model.NestedNodeInfo;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import static pl.exsio.nestedj.util.NestedNodeUtil.*;

public class JpaNestedNodeMover<ID extends Serializable, N extends NestedNode<ID, N>> extends JpaNestedNodeDelegate<ID, N> implements NestedNodeMover<ID, N> {

    private enum Sign {
        PLUS, MINUS
    }

    public JpaNestedNodeMover(EntityManager entityManager, TreeDiscriminator<ID, N> treeDiscriminator) {
        super(entityManager, treeDiscriminator);
    }

    @Override
    public void move(NestedNodeInfo<ID, N> nodeInfo, NestedNodeInfo<ID, N> parentInfo, Mode mode) {
        Class<N> nodeClass = nodeInfo.getNodeClass();
        if (!canMoveNodeToSelectedParent(nodeInfo, parentInfo)) {
            throw new InvalidNodesHierarchyException("You cannot move a parent node to it's child or move a node to itself");
        }
        List<Long> nodeIds = getNodeIds(nodeInfo);

        Sign sign = getSign(nodeInfo, parentInfo, mode);
        Long start = getStart(nodeInfo, parentInfo, mode, sign);
        Long stop = getStop(nodeInfo, parentInfo, mode, sign);
        Long delta = getDelta(nodeIds);
        makeSpaceForMovedElement(sign, delta, start, stop, nodeClass);

        Long nodeDelta = getNodeDelta(start, stop);
        Sign nodeSign = getNodeSign(sign);
        Long levelModificator = getLevelModificator(nodeInfo, parentInfo, mode);
        performMove(nodeSign, nodeDelta, nodeIds, levelModificator, nodeClass);

        Optional<N> newParent = getNewParent(parentInfo, mode);
        updateParentField(newParent, nodeInfo, nodeClass);
    }

    private void makeSpaceForMovedElement(Sign sign, Long delta, Long start, Long stop, Class<N> nodeClass) {
        updateFields(sign, delta, start, stop, nodeClass, right(nodeClass));
        updateFields(sign, delta, start, stop, nodeClass, left(nodeClass));
    }

    private void updateParentField(Optional<N> newParent, NestedNodeInfo<ID, N> node, Class<N> nodeClass) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<N> update = cb.createCriteriaUpdate(nodeClass);
        Root<N> root = update.from(nodeClass);

        update.set(root.get(parent(nodeClass)), newParent.orElse(null))
                .where(getPredicates(cb, root, cb.equal(root.get(id(nodeClass)), node.getId())));

        entityManager.createQuery(update).executeUpdate();
    }

    private void performMove(Sign nodeSign, Long nodeDelta, List nodeIds, Long levelModificator, Class<N> nodeClass) {
        if (!nodeIds.isEmpty()) {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaUpdate<N> update = cb.createCriteriaUpdate(nodeClass);
            Root<N> root = update.from(nodeClass);

            update.set(root.<Long>get(level(nodeClass)), cb.sum(root.get(level(nodeClass)), levelModificator));
            if (Sign.MINUS.equals(nodeSign)) {
                update.set(root.<Long>get(right(nodeClass)), cb.diff(root.get(right(nodeClass)), nodeDelta));
                update.set(root.<Long>get(left(nodeClass)), cb.diff(root.get(left(nodeClass)), nodeDelta));
            } else if (Sign.PLUS.equals(nodeSign)) {
                update.set(root.<Long>get(right(nodeClass)), cb.sum(root.get(right(nodeClass)), nodeDelta));
                update.set(root.<Long>get(left(nodeClass)), cb.sum(root.get(left(nodeClass)), nodeDelta));
            }
            update.where(getPredicates(cb, root, root.get(id(nodeClass)).in(nodeIds)));

            entityManager.createQuery(update).executeUpdate();
        }
    }

    private void updateFields(Sign sign, Long delta, Long start, Long stop, Class<N> nodeClass, String field) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<N> update = cb.createCriteriaUpdate(nodeClass);
        Root<N> root = update.from(nodeClass);

        if (Sign.MINUS.equals(sign)) {
            update.set(root.<Long>get(field), cb.diff(root.get(field), delta));
        } else if (Sign.PLUS.equals(sign)) {
            update.set(root.<Long>get(field), cb.sum(root.get(field), delta));
        }
        update.where(getPredicates(cb, root,
                cb.greaterThan(root.get(field), start),
                cb.lessThan(root.get(field), stop)
        ));

        entityManager.createQuery(update).executeUpdate();
    }

    private boolean canMoveNodeToSelectedParent(NestedNodeInfo<ID, N> node, NestedNodeInfo<ID, N> parent) {
        return !node.getId().equals(parent.getId()) && (node.getLeft() >= parent.getLeft() || node.getRight() <= parent.getRight());
    }

    private List<Long> getNodeIds(NestedNodeInfo<ID, N> node) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> select = cb.createQuery(Long.class);
        Root<N> root = select.from(node.getNodeClass());
        select.select(root.get(id(node.getNodeClass()))).where(
                getPredicates(cb, root,
                        cb.greaterThanOrEqualTo(root.get(left(node.getNodeClass())), node.getLeft()),
                        cb.lessThanOrEqualTo(root.get(right(node.getNodeClass())), node.getRight())
                ));
        return entityManager.createQuery(select).getResultList();
    }

    private Optional<N> getNewParent(NestedNodeInfo<ID, N> parent, Mode mode) {
        switch (mode) {
            case NEXT_SIBLING:
            case PREV_SIBLING:
                if (parent.getParentId() != null) {
                    return Optional.of(entityManager.getReference(parent.getNodeClass(), parent.getParentId()));
                } else {
                    return Optional.empty();
                }
            case FIRST_CHILD:
            case LAST_CHILD:
            default:
                return Optional.of(entityManager.getReference(parent.getNodeClass(), parent.getId()));
        }
    }

    private Long getLevelModificator(NestedNodeInfo<ID, N> node, NestedNodeInfo<ID, N> parent, Mode mode) {
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

    private Sign getSign(NestedNodeInfo<ID, N> node, NestedNodeInfo<ID, N> parent, Mode mode) {
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

    private Long getStart(NestedNodeInfo<ID, N> node, NestedNodeInfo<ID, N> parent, Mode mode, Sign sign) {
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

    private Long getStop(NestedNodeInfo<ID, N> node, NestedNodeInfo<ID, N> parent, Mode mode, Sign sign) {
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

}
