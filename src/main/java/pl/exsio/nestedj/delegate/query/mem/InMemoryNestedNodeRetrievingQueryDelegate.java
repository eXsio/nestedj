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
import pl.exsio.nestedj.delegate.query.NestedNodeRetrievingQueryDelegate;
import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.model.NestedNodeInfo;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static pl.exsio.nestedj.model.NestedNode.ID;
import static pl.exsio.nestedj.model.NestedNode.LEFT;
import static pl.exsio.nestedj.model.NestedNode.LEVEL;
import static pl.exsio.nestedj.model.NestedNode.PARENT_ID;
import static pl.exsio.nestedj.model.NestedNode.RIGHT;

public class InMemoryNestedNodeRetrievingQueryDelegate<ID extends Serializable, N extends NestedNode<ID>>
        extends InMemoryNestedNodeQueryDelegate<ID, N>
        implements NestedNodeRetrievingQueryDelegate<ID, N> {

    public InMemoryNestedNodeRetrievingQueryDelegate(InMemoryNestedNodeRepositoryConfiguration<ID, N> configuration) {
        super(configuration);
    }


    @Override
    public List<N> getTreeAsList(N node) {
        return nodesStream()
                .filter(n -> getLong(LEFT, n) >= node.getTreeLeft())
                .filter(n -> getLong(RIGHT, n) <= node.getTreeRight())
                .sorted(Comparator.comparing(NestedNode::getTreeLeft))
                .collect(Collectors.toList());
    }

    @Override
    public List<N> getChildren(N node) {
        return nodesStream()
                .filter(n -> getLong(LEFT, n) >= node.getTreeLeft())
                .filter(n -> getLong(RIGHT, n) <= node.getTreeRight())
                .filter(n -> getLong(LEVEL, n).equals(node.getTreeLevel() + 1))
                .sorted(Comparator.comparing(NestedNode::getTreeLeft))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<N> getParent(N node) {
        if (node.getTreeLevel() > 0) {
            return nodesStream()
                    .filter(n -> getLong(LEFT, n) < node.getTreeLeft())
                    .filter(n -> getLong(RIGHT, n) > node.getTreeRight())
                    .filter(n -> getLong(LEVEL, n).equals(node.getTreeLevel() - 1))
                    .min(Comparator.comparing(NestedNode::getTreeLeft));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<N> getParents(N node) {
        return nodesStream()
                .filter(n -> getLong(LEFT, n) < node.getTreeLeft())
                .filter(n -> getLong(RIGHT, n) > node.getTreeRight())
                .sorted(Comparator.<N, Long>comparing(NestedNode::getTreeLeft).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public Optional<N> getPrevSibling(N node) {
        return nodesStream()
                .filter(n -> getLong(RIGHT, n).equals(node.getTreeLeft() - 1))
                .filter(n -> getLong(LEVEL, n).equals(node.getTreeLevel()))
                .min(Comparator.comparing(NestedNode::getTreeLeft));
    }

    @Override
    public Optional<N> getNextSibling(N node) {
        return nodesStream()
                .filter(n -> getLong(LEFT, n).equals(node.getTreeRight() + 1))
                .filter(n -> getLong(LEVEL, n).equals(node.getTreeLevel()))
                .min(Comparator.comparing(NestedNode::getTreeLeft));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<NestedNodeInfo<ID>> getNodeInfo(ID nodeId) {
        Optional<N> node = nodesStream()
                .filter(n -> getSerializable(ID, n).equals(nodeId))
                .findFirst();
        return node.map(n -> new NestedNodeInfo<>(
                        (ID) getSerializable(ID, n),
                        (ID) getSerializable(PARENT_ID, n),
                        getLong(LEFT, n),
                        getLong(RIGHT, n),
                        getLong(LEVEL, n)
                ));
    }

    @Override
    public Optional<N> findFirstRoot() {
        return nodesStream()
                .filter(n -> getLong(LEVEL, n).equals(0L))
                .min(Comparator.comparing(NestedNode::getTreeLeft));
    }

    @Override
    public Optional<N> findLastRoot() {
        return nodesStream()
                .filter(n -> getLong(LEVEL, n).equals(0L))
                .max(Comparator.comparing(NestedNode::getTreeLeft));
    }
}
