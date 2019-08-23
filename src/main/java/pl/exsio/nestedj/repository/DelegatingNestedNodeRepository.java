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
import pl.exsio.nestedj.delegate.*;
import pl.exsio.nestedj.ex.InvalidNodeException;
import pl.exsio.nestedj.ex.InvalidParentException;
import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.model.NestedNodeInfo;
import pl.exsio.nestedj.model.Tree;

import java.io.Serializable;
import java.util.Optional;

public class DelegatingNestedNodeRepository<ID extends Serializable, N extends NestedNode<ID>> implements NestedNodeRepository<ID, N> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DelegatingNestedNodeRepository.class);

    private final Class<ID> idClass;

    private final Class<N> nodeClass;

    private final NestedNodeInserter<ID, N> inserter;

    private final NestedNodeMover<ID, N> mover;

    private final NestedNodeRemover<ID, N> remover;

    private final NestedNodeRetriever<ID, N> retriever;

    private final NestedNodeRebuilder<ID, N> rebuilder;

    private boolean allowNullableTreeFields = false;


    public DelegatingNestedNodeRepository(Class<ID> idClass, Class<N> nodeClass, NestedNodeMover<ID, N> mover, NestedNodeRemover<ID, N> remover, NestedNodeRetriever<ID, N> retriever, NestedNodeRebuilder<ID, N> rebuilder, NestedNodeInserter<ID, N> inserter) {
        this.idClass = idClass;
        this.nodeClass = nodeClass;
        this.inserter = inserter;
        this.mover = mover;
        this.remover = remover;
        this.retriever = retriever;
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
        Optional<NestedNodeInfo<ID, N>> parentInfo = retriever.getNodeInfo(parent.getId(), nodeClass, idClass);
        if (!parentInfo.isPresent()) {
            throw new InvalidParentException(String.format("Cannot insert or move to non existent parent. Parent id: %s", parent.getId()));
        }
        if (node.getId() != null) {
            Optional<NestedNodeInfo<ID, N>> nodeInfo = retriever.getNodeInfo(node.getId(), nodeClass, idClass);
            if (nodeInfo.isPresent()) {
                boolean nodeInfoValid = isNodeInfoValid(nodeInfo.get());
                if (nodeInfoValid) {
                    this.mover.move(nodeInfo.get(), parentInfo.get(), mode);
                } else if (allowNullableTreeFields) {
                    LOGGER.warn("Nullable tree fields allowed. Trying to perform an insert on an existing, invalid tree node: {}", nodeInfo.get());
                    this.inserter.insert(node, parentInfo.get(), mode, nodeClass);
                } else {
                    throw new InvalidNodeException(String.format("Current configuration doesn't allow nullable tree fields: %s", nodeInfo.get()));
                }
            } else {
                this.inserter.insert(node, parentInfo.get(), mode, nodeClass);
            }
        } else {
            this.inserter.insert(node, parentInfo.get(), mode, nodeClass);
        }
    }

    private boolean isNodeInfoValid(NestedNodeInfo<ID, N> nodeInfo) {
        return (nodeInfo.getLeft() != null && nodeInfo.getRight() != null);
    }

    @Override
    public void removeSingle(N node) {
        Optional<NestedNodeInfo<ID, N>> nodeInfo = retriever.getNodeInfo(node.getId(), nodeClass, idClass);
        if (nodeInfo.isPresent()) {
            this.remover.removeSingle(nodeInfo.get());
        } else {
            throw new InvalidNodeException(String.format("Couldn't remove node, was it already removed?: %s", node));
        }

    }

    @Override
    public void removeSubtree(N node) {
        Optional<NestedNodeInfo<ID, N>> nodeInfo = retriever.getNodeInfo(node.getId(), nodeClass, idClass);
        if (nodeInfo.isPresent()) {
            this.remover.removeSubtree(nodeInfo.get());
        } else {
            throw new InvalidNodeException(String.format("Couldn't remove node subtree, was it already removed?: %s", node));
        }
    }

    @Override
    public Iterable<N> getTreeAsList(N node) {
        return this.retriever.getTreeAsList(node, nodeClass);
    }

    @Override
    public Iterable<N> getChildren(N node) {
        return this.retriever.getChildren(node, nodeClass);
    }

    @Override
    public Optional<N> getParent(N node) {
        return this.retriever.getParent(node, nodeClass);
    }

    @Override
    public Optional<N> getPrevSibling(N node) {
        return this.retriever.getPrevSibling(node, nodeClass);
    }

    @Override
    public Optional<N> getNextSibling(N node) {
        return this.retriever.getNextSibling(node, nodeClass);
    }

    @Override
    public Tree<ID, N> getTree(N node) {
        return this.retriever.getTree(node, nodeClass);
    }

    @Override
    public Iterable<N> getParents(N node) {
        return this.retriever.getParents(node, nodeClass);
    }

    @Override
    public void rebuildTree() {
        this.rebuilder.rebuildTree(nodeClass, idClass);
    }

    @Override
    public void destroyTree() {
        rebuilder.destroyTree(nodeClass);
    }

    public boolean isAllowNullableTreeFields() {
        return allowNullableTreeFields;
    }

    public void setAllowNullableTreeFields(boolean allowNullableTreeFields) {
        this.allowNullableTreeFields = allowNullableTreeFields;
    }
}
