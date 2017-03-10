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
package pl.exsio.nestedj.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.exsio.nestedj.delegate.NestedNodeHierarchyManipulator;
import pl.exsio.nestedj.delegate.NestedNodeInserter;
import pl.exsio.nestedj.delegate.NestedNodeMover;
import pl.exsio.nestedj.delegate.NestedNodeRebuilder;
import pl.exsio.nestedj.delegate.NestedNodeRemover;
import pl.exsio.nestedj.delegate.NestedNodeRetriever;
import pl.exsio.nestedj.ex.InvalidNodeException;
import pl.exsio.nestedj.ex.InvalidParentException;
import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.model.NestedNodeInfo;
import pl.exsio.nestedj.model.Tree;

import java.util.Optional;

public class NestedNodeRepositoryImpl<N extends NestedNode<N>> implements NestedNodeRepository<N> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NestedNodeRepositoryImpl.class);

    private NestedNodeInserter<N> inserter;

    private NestedNodeMover<N> mover;

    private NestedNodeRemover<N> remover;

    private NestedNodeRetriever<N> retriever;

    private NestedNodeRebuilder<N> rebuilder;

    private boolean allowNullableTreeFields = false;

    public void setInserter(NestedNodeInserter<N> inserter) {
        this.inserter = inserter;
    }

    public void setMover(NestedNodeMover<N> mover) {
        this.mover = mover;
    }

    public void setRemover(NestedNodeRemover<N> remover) {
        this.remover = remover;
    }

    public void setRetriever(NestedNodeRetriever<N> retriever) {
        this.retriever = retriever;
    }

    public void setRebuilder(NestedNodeRebuilder<N> rebuilder) {
        this.rebuilder = rebuilder;
    }

    @Override
    public void insertAsFirstChildOf(N node, N parent) {
        this.insertOrMove(node, parent, NestedNodeHierarchyManipulator.Mode.FIRST_CHILD);
    }

    @Override
    public void insertAsLastChildOf(N node, N parent) {
        this.insertOrMove(node, parent, NestedNodeHierarchyManipulator.Mode.LAST_CHILD);
    }

    @Override
    public void insertAsNextSiblingOf(N node, N parent) {
        this.insertOrMove(node, parent, NestedNodeHierarchyManipulator.Mode.NEXT_SIBLING);
    }

    @Override
    public void insertAsPrevSiblingOf(N node, N parent) {
        this.insertOrMove(node, parent, NestedNodeHierarchyManipulator.Mode.PREV_SIBLING);
    }

    private void insertOrMove(N node, N parent, NestedNodeHierarchyManipulator.Mode mode) {
        if (parent.getId() == null) {
            throw new InvalidParentException("Cannot insert or move to a parent that has null id");
        }
        Optional<NestedNodeInfo<N>> parentInfo = retriever.getNodeInfo(parent.getId(), getNodeClass(parent));
        if (!parentInfo.isPresent()) {
            throw new InvalidParentException(String.format("Cannot insert or move to non existent parent. Parent id: %d", parent.getId()));
        }
        if (node.getId() != null) {
            Optional<NestedNodeInfo<N>> nodeInfo = retriever.getNodeInfo(node.getId(), getNodeClass(node));
            if (nodeInfo.isPresent()) {
                boolean nodeInfoValid = isNodeInfoValid(nodeInfo);
                if (nodeInfoValid) {
                    this.mover.move(nodeInfo.get(), parentInfo.get(), mode);
                } else if (allowNullableTreeFields) {
                    LOGGER.warn("Nullable tree fields allowed. Trying to perform an insert on an existing, invalid tree node: {}", nodeInfo.get());
                    this.inserter.insert(node, parentInfo.get(), mode);
                } else {
                    throw new InvalidNodeException(String.format("Current configuration doesn't allow nullable tree fields: %s", nodeInfo.get()));
                }
            } else {
                this.inserter.insert(node, parentInfo.get(), mode);
            }
        } else {
            this.inserter.insert(node, parentInfo.get(), mode);
        }
    }

    private boolean isNodeInfoValid(Optional<NestedNodeInfo<N>> nodeInfo) {
        return (nodeInfo.get().getLeft() != null && nodeInfo.get().getRight() != null);
    }

    @Override
    public void removeSingle(N node) {
        Optional<NestedNodeInfo<N>> nodeInfo = retriever.getNodeInfo(node.getId(), getNodeClass(node));
        if (nodeInfo.isPresent()) {
            this.remover.removeSingle(nodeInfo.get());
        } else {
            throw new InvalidNodeException(String.format("Couldn't remove node, was it already removed?: %s", node));
        }

    }

    @Override
    public void removeSubtree(N node) {
        Optional<NestedNodeInfo<N>> nodeInfo = retriever.getNodeInfo(node.getId(), getNodeClass(node));
        if (nodeInfo.isPresent()) {
            this.remover.removeSubtree(nodeInfo.get());
        } else {
            throw new InvalidNodeException(String.format("Couldn't remove node subtree, was it already removed?: %s", node));
        }
    }

    @Override
    public Iterable<N> getTreeAsList(N node) {
        return this.retriever.getTreeAsList(node);
    }

    @Override
    public Iterable<N> getChildren(N node) {
        return this.retriever.getChildren(node);
    }

    @Override
    public Optional<N> getParent(N node) {
        return this.retriever.getParent(node);
    }

    @Override
    public Tree<N> getTree(N node) {
        return this.retriever.getTree(node);
    }

    @Override
    public Iterable<N> getParents(N node) {
        return this.retriever.getParents(node);
    }

    @Override
    public void rebuildTree(Class<N> nodeClass) {
        this.rebuilder.rebuildTree(nodeClass);
    }

    @Override
    public void destroyTree(Class<N> nodeClass) {
        rebuilder.destroyTree(nodeClass);
    }

    public boolean isAllowNullableTreeFields() {
        return allowNullableTreeFields;
    }

    public void setAllowNullableTreeFields(boolean allowNullableTreeFields) {
        this.allowNullableTreeFields = allowNullableTreeFields;
    }

    private Class<N> getNodeClass(N node) {
        return (Class<N>) node.getClass();
    }
}
