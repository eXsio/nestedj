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
package pl.exsio.nestedj.delegate.control;

import pl.exsio.nestedj.delegate.NestedNodeMover;
import pl.exsio.nestedj.delegate.query.NestedNodeMovingQueryDelegate;
import pl.exsio.nestedj.ex.InvalidNodesHierarchyException;
import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.model.NestedNodeInfo;

import java.io.Serializable;
import java.util.Optional;

import static pl.exsio.nestedj.model.NestedNode.LEFT;
import static pl.exsio.nestedj.model.NestedNode.RIGHT;

public class QueryBasedNestedNodeMover<ID extends Serializable, N extends NestedNode<ID>> implements NestedNodeMover<ID, N> {

    private final static long DELTA_MULTIPLIER = 2L;

    private enum Sign {
        PLUS, MINUS
    }

    private final NestedNodeMovingQueryDelegate<ID, N> queryDelegate;

    public QueryBasedNestedNodeMover(NestedNodeMovingQueryDelegate<ID, N> queryDelegate) {
        this.queryDelegate = queryDelegate;
    }

    @Override
    public void move(NestedNodeInfo<ID> nodeInfo, NestedNodeInfo<ID> parentInfo, Mode mode) {
        if (!canMoveNodeToSelectedParent(nodeInfo, parentInfo)) {
            throw new InvalidNodesHierarchyException("You cannot move a parent node to it's child or move a node to itself");
        }
        Integer nodeCount = queryDelegate.markNodeIds(nodeInfo);

        Sign sign = getSign(nodeInfo, parentInfo, mode);
        Long start = getStart(nodeInfo, parentInfo, mode, sign);
        Long stop = getStop(nodeInfo, parentInfo, mode, sign);
        Long delta = getDelta(nodeCount);
        makeSpaceForMovedElement(sign, delta, start, stop);

        Long nodeDelta = getNodeDelta(start, stop);
        Sign nodeSign = getNodeSign(sign);
        Long levelModificator = getLevelModificator(nodeInfo, parentInfo, mode);
        performMove(nodeDelta, nodeSign, levelModificator);
        updateParent(nodeInfo, parentInfo, mode);
    }

    private void updateParent(NestedNodeInfo<ID> nodeInfo, NestedNodeInfo<ID> parentInfo, Mode mode) {
        Optional<ID> newParent = getNewParentId(parentInfo, mode);
        if (newParent.isPresent()) {
            queryDelegate.updateParentField(newParent.get(), nodeInfo);
        } else {
            queryDelegate.clearParentField(nodeInfo);
        }
    }

    private void performMove(Long nodeDelta, Sign nodeSign, Long levelModificator) {
        if(Sign.PLUS.equals(nodeSign)) {
            queryDelegate.performMoveUp(nodeDelta, levelModificator);
        } else if(Sign.MINUS.equals(nodeSign)) {
            queryDelegate.performMoveDown(nodeDelta, levelModificator);
        }
    }

    private void makeSpaceForMovedElement(Sign sign, Long delta, Long start, Long stop) {
        if(Sign.PLUS.equals(sign)) {
            queryDelegate.updateFieldsUp(delta, start, stop, RIGHT);
            queryDelegate.updateFieldsUp(delta, start, stop, LEFT);
        } else if(Sign.MINUS.equals(sign)) {
            queryDelegate.updateFieldsDown(delta, start, stop, RIGHT);
            queryDelegate.updateFieldsDown(delta, start, stop, LEFT);
        }
    }

    private boolean canMoveNodeToSelectedParent(NestedNodeInfo<ID> node, NestedNodeInfo<ID> parent) {
        return !node.getId().equals(parent.getId()) && (node.getLeft() >= parent.getLeft() || node.getRight() <= parent.getRight());
    }

    private Optional<ID> getNewParentId(NestedNodeInfo<ID> parent, Mode mode) {
        switch (mode) {
            case NEXT_SIBLING:
            case PREV_SIBLING:
                if (parent.getParentId() != null) {
                    return Optional.of(parent.getParentId());
                } else {
                    return Optional.empty();
                }
            case FIRST_CHILD:
            case LAST_CHILD:
            default:
                return Optional.of(parent.getId());
        }
    }

    private Long getLevelModificator(NestedNodeInfo<ID> node, NestedNodeInfo<ID> parent, Mode mode) {
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

    private Long getDelta(Integer nodeCount) {
        return nodeCount * DELTA_MULTIPLIER;
    }

    private Sign getNodeSign(Sign sign) {
        return (sign.equals(Sign.PLUS)) ? Sign.MINUS : Sign.PLUS;
    }

    private Sign getSign(NestedNodeInfo<ID> node, NestedNodeInfo<ID> parent, Mode mode) {
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

    private Long getStart(NestedNodeInfo<ID> node, NestedNodeInfo<ID> parent, Mode mode, Sign sign) {
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

    private Long getStop(NestedNodeInfo<ID> node, NestedNodeInfo<ID> parent, Mode mode, Sign sign) {
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
