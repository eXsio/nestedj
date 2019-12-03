/*
 *  The MIT License
 *
 *  Copyright (c) 2019 eXsio.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 *  documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 *  rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 *  permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *  The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 *  the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 *  BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 *  CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 *  ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package pl.exsio.nestedj.delegate.query.mem;

import pl.exsio.nestedj.config.mem.InMemoryNestedNodeRepositoryConfiguration;
import pl.exsio.nestedj.delegate.query.NestedNodeRemovingQueryDelegate;
import pl.exsio.nestedj.ex.InvalidNodeException;
import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.model.NestedNodeInfo;

import java.io.Serializable;
import java.util.Optional;

import static pl.exsio.nestedj.model.NestedNode.*;

public class InMemoryNestedNodeRemovingQueryDelegate<ID extends Serializable, N extends NestedNode<ID>>
        extends InMemoryNestedNodeQueryDelegate<ID, N>
        implements NestedNodeRemovingQueryDelegate<ID, N> {

    public InMemoryNestedNodeRemovingQueryDelegate(InMemoryNestedNodeRepositoryConfiguration<ID, N> configuration) {
        super(configuration);
    }


    @Override
    public void setNewParentForDeletedNodesChildren(NestedNodeInfo<ID> node) {
        nodesStream()
                .filter(n -> getLong(LEFT, n) >= node.getLeft())
                .filter(n -> getLong(RIGHT, n) <= node.getRight())
                .filter(n -> getLong(LEVEL, n).equals(node.getLevel() + 1))
                .forEach(n -> setSerializable(PARENT_ID, n, findNodeParentId(node).orElse(null)));
    }

    @Override
    public void performSingleDeletion(NestedNodeInfo<ID> node) {
        nodesStream()
                .filter(n -> getSerializable(ID, n).equals(node.getId()))
                .forEach(nodes::remove);
    }

    @Override
    public void decrementSideFieldsBeforeSingleNodeRemoval(Long from, String field) {
        decrementSideFields(from, DECREMENT_BY, field);
    }

    @Override
    public void pushUpDeletedNodesChildren(NestedNodeInfo<ID> node) {
        nodesStream()
                .filter(n -> getLong(LEFT, n) > node.getLeft())
                .filter(n -> getLong(RIGHT, n) < node.getRight())
                .forEach(n -> {
                    setLong(RIGHT, n , getLong(RIGHT, n) - 1);
                    setLong(LEFT, n , getLong(LEFT, n) - 1);
                    setLong(LEVEL, n , getLong(LEVEL, n) - 1);
                });
    }

    @Override
    public void decrementSideFieldsAfterSubtreeRemoval(Long from, Long delta, String field) {
        decrementSideFields(from, delta, field);
    }

    @Override
    public void performBatchDeletion(NestedNodeInfo<ID> node) {
        nodesStream()
                .filter(n -> getLong(LEFT, n) >= node.getLeft())
                .filter(n -> getLong(RIGHT, n) <= node.getRight())
                .forEach(nodes::remove);
    }

    private void decrementSideFields(Long from, Long delta, String field) {
        nodesStream()
                .filter(n -> getLong(field, n) > from)
                .forEach(n -> setLong(field, n , getLong(field, n) - delta));
    }

    private Optional<ID> findNodeParentId(NestedNodeInfo<ID> node) {
        if (node.getLevel() > 0) {
            return Optional.of(nodesStream()
                    .filter(n -> getLong(LEFT, n) < node.getLeft())
                    .filter(n -> getLong(RIGHT, n) > node.getRight())
                    .filter(n -> getLong(LEVEL, n).equals(node.getLevel() - 1))
                    .map(NestedNode::getId)
                    .findFirst()
                    .orElseThrow(() -> new InvalidNodeException(String.format("Couldn't find node's parent, although its level is greater than 0. It seems the tree is malformed: %s", node))));
        }
        return Optional.empty();
    }
}
