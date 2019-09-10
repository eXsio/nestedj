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

import pl.exsio.nestedj.delegate.NestedNodeRemover;
import pl.exsio.nestedj.delegate.query.NestedNodeRemovingQueryDelegate;
import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.model.NestedNodeInfo;

import java.io.Serializable;

import static pl.exsio.nestedj.model.NestedNode.LEFT;
import static pl.exsio.nestedj.model.NestedNode.RIGHT;

public class QueryBasedNestedNodeRemover<ID extends Serializable, N extends NestedNode<ID>> implements NestedNodeRemover<ID, N> {

    private final NestedNodeRemovingQueryDelegate<ID, N> queryDelegate;

    public QueryBasedNestedNodeRemover(NestedNodeRemovingQueryDelegate<ID, N> queryDelegate) {
        this.queryDelegate = queryDelegate;
    }

    @Override
    public void removeSingle(NestedNodeInfo<ID> nodeInfo) {
        Long from = nodeInfo.getRight();
        queryDelegate.setNewParentForDeletedNodesChildren(nodeInfo);
        queryDelegate.decrementSideFieldsBeforeSingleNodeRemoval(from, RIGHT);
        queryDelegate.decrementSideFieldsBeforeSingleNodeRemoval(from, LEFT);
        queryDelegate.pushUpDeletedNodesChildren(nodeInfo);
        queryDelegate.performSingleDeletion(nodeInfo);
    }

    @Override
    public void removeSubtree(NestedNodeInfo<ID> nodeInfo) {
        Long delta = nodeInfo.getRight() - nodeInfo.getLeft() + 1;
        Long from = nodeInfo.getRight();
        queryDelegate.performBatchDeletion(nodeInfo);
        queryDelegate.decrementSideFieldsAfterSubtreeRemoval(from, delta, RIGHT);
        queryDelegate.decrementSideFieldsAfterSubtreeRemoval(from, delta, LEFT);
    }
}
