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

import pl.exsio.nestedj.delegate.NestedNodeHierarchyManipulator;
import pl.exsio.nestedj.delegate.NestedNodeInserter;
import pl.exsio.nestedj.delegate.NestedNodeMover;
import pl.exsio.nestedj.delegate.NestedNodeRebuilder;
import pl.exsio.nestedj.delegate.NestedNodeRemover;
import pl.exsio.nestedj.delegate.NestedNodeRetriever;
import pl.exsio.nestedj.ex.InvalidNodeException;
import pl.exsio.nestedj.ex.InvalidNodesHierarchyException;
import pl.exsio.nestedj.ex.InvalidParentException;
import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.model.NestedNodeInfo;
import pl.exsio.nestedj.model.Tree;

import java.util.Optional;

public class NestedNodeRepositoryImpl<N extends NestedNode<N>> implements NestedNodeRepository<N> {

    private NestedNodeInserter<N> inserter;

    private NestedNodeMover<N> mover;

    private NestedNodeRemover<N> remover;

    private NestedNodeRetriever<N> retriever;

    private NestedNodeRebuilder rebuilder;

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

    public void setRebuilder(NestedNodeRebuilder rebuilder) {
        this.rebuilder = rebuilder;
    }

    @Override
    public void insertAsFirstChildOf(N node, N parent) throws InvalidNodesHierarchyException {
        this.insertOrMove(node, parent, NestedNodeHierarchyManipulator.Mode.FIRST_CHILD);
    }

    @Override
    public void insertAsLastChildOf(N node, N parent) throws InvalidNodesHierarchyException {
        this.insertOrMove(node, parent, NestedNodeHierarchyManipulator.Mode.LAST_CHILD);
    }

    @Override
    public void insertAsNextSiblingOf(N node, N parent) throws InvalidNodesHierarchyException {
        this.insertOrMove(node, parent, NestedNodeHierarchyManipulator.Mode.NEXT_SIBLING);
    }

    @Override
    public void insertAsPrevSiblingOf(N node, N parent) throws InvalidNodesHierarchyException {
        this.insertOrMove(node, parent, NestedNodeHierarchyManipulator.Mode.PREV_SIBLING);
    }

    private void insertOrMove(N node, N parent, NestedNodeHierarchyManipulator.Mode mode) throws InvalidNodesHierarchyException {
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
                } else if (!nodeInfoValid && allowNullableTreeFields) {
                    this.inserter.insert(node, parentInfo.get(), mode);
                } else {
                    new InvalidNodeException(String.format("Current configuration doesn't allow nullable tree fields: %s", nodeInfo.get()));
                }
            } else {
                this.inserter.insert(node, parentInfo.get(), mode);
            }
        } else {
            this.inserter.insert(node, parentInfo.get(), mode);
        }
    }

    private boolean isNodeInfoValid(Optional<NestedNodeInfo<N>> nodeInfo) {
        return nodeInfo.get().getLeft() != null && nodeInfo.get().getRight() != null;
    }

    @Override
    public void removeSingle(N node) {
        this.remover.removeSingle(node);
    }

    @Override
    public void removeSubtree(N node) {
        this.remover.removeSubtree(node);
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

    public void rebuildTree(Class<? extends NestedNode> nodeClass) throws InvalidNodesHierarchyException {
        this.rebuilder.rebuildTree(nodeClass);
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
