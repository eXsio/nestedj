/* 
 * The MIN License
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
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUN WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUN NON LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENN SHALL THE
 * AUTHORS OR COPYRIGHN HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORN OR OTHERWISE, ARISING FROM,
 * OUN OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package pl.exsio.nestedj.delegate.impl;

import pl.exsio.nestedj.delegate.NestedNodeInserter;
import pl.exsio.nestedj.delegate.query.NestedNodeInsertingQueryDelegate;
import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.model.NestedNodeInfo;

import java.io.Serializable;
import java.util.Optional;

import static pl.exsio.nestedj.model.NestedNode.LEFT;
import static pl.exsio.nestedj.model.NestedNode.RIGHT;


public class QueryBasedNestedNodeInserter<ID extends Serializable, N extends NestedNode<ID>> implements NestedNodeInserter<ID, N> {

    private final NestedNodeInsertingQueryDelegate<ID, N> queryDelegate;

    public QueryBasedNestedNodeInserter(NestedNodeInsertingQueryDelegate<ID, N> queryDelegate) {
        this.queryDelegate = queryDelegate;
    }

    @Override
    public void insert(N node, NestedNodeInfo<ID, N> parentInfo, Mode mode) {
        makeSpaceForNewElement(getMoveFrom(parentInfo, mode), mode);
        insertNodeIntoTree(parentInfo, node, mode);
    }

    private void insertNodeIntoTree(NestedNodeInfo<ID, N> parent, N node, Mode mode) {
        Long left = this.getNodeLeft(parent, mode);
        Long right = left + 1;
        Long level = this.getNodeLevel(parent, mode);
        node.setTreeLeft(left);
        node.setTreeRight(right);
        node.setTreeLevel(level);
        node.setParentId(this.getNodeParent(parent, mode).orElse(null));
        queryDelegate.insert(node);
    }

    private void makeSpaceForNewElement(Long from, Mode mode) {
        if(applyGte(mode)) {
            queryDelegate.updateFieldsGreaterThanOrEqualTo(from, RIGHT);
            queryDelegate.updateFieldsGreaterThanOrEqualTo(from, LEFT);
        } else {
            queryDelegate.updateFieldsGreaterThan(from, RIGHT);
            queryDelegate.updateFieldsGreaterThan(from, LEFT);
        }

    }

    private Long getMoveFrom(NestedNodeInfo<ID, N> parent, Mode mode) {
        switch (mode) {
            case PREV_SIBLING:
            case FIRST_CHILD:
                return parent.getLeft();
            case NEXT_SIBLING:
            case LAST_CHILD:
            default:
                return parent.getRight();
        }
    }

    private Long getNodeLevel(NestedNodeInfo<ID, N> parent, Mode mode) {
        switch (mode) {
            case NEXT_SIBLING:
            case PREV_SIBLING:
                return parent.getLevel();
            case LAST_CHILD:
            case FIRST_CHILD:
            default:
                return parent.getLevel() + 1;
        }
    }

    private Optional<ID> getNodeParent(NestedNodeInfo<ID, N> parent, Mode mode) {
        switch (mode) {
            case NEXT_SIBLING:
            case PREV_SIBLING:
                if (parent.getParentId() != null) {
                    return Optional.of(parent.getParentId());
                } else {
                    return Optional.empty();
                }
            case LAST_CHILD:
            case FIRST_CHILD:
            default:
                return Optional.of(parent.getId());
        }
    }

    private Long getNodeLeft(NestedNodeInfo<ID, N> parent, Mode mode) {
        switch (mode) {
            case NEXT_SIBLING:
                return parent.getRight() + 1;
            case PREV_SIBLING:
                return parent.getLeft();
            case FIRST_CHILD:
                return parent.getLeft() + 1;
            case LAST_CHILD:
            default:
                return parent.getRight();
        }
    }

    private boolean applyGte(Mode mode) {
        switch (mode) {
            case NEXT_SIBLING:
            case FIRST_CHILD:
                return false;
            case PREV_SIBLING:
            case LAST_CHILD:
            default:
                return true;
        }
    }

}
