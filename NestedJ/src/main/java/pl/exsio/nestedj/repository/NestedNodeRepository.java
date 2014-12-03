/* 
 * The MIT License
 *
 * Copyright 2014 exsio.
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
package pl.exsio.nestedj.repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import pl.exsio.nestedj.NestedNodeInserter;
import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.NestedNodeUtil;
import pl.exsio.nestedj.NestedNodeHierarchyManipulator;
import pl.exsio.nestedj.NestedNodeMover;
import pl.exsio.nestedj.NestedNodeRebuilder;
import pl.exsio.nestedj.NestedNodeRemover;
import pl.exsio.nestedj.NestedNodeRetriever;
import pl.exsio.nestedj.dao.NestedNodeDao;
import pl.exsio.nestedj.ex.InvalidNodesHierarchyException;
import pl.exsio.nestedj.model.Tree;

/**
 *
 * @author exsio
 * @param <T>
 */
public class NestedNodeRepository<T extends NestedNode> implements NestedNodeDao<T> {

    @PersistenceContext
    private EntityManager em;

    private NestedNodeUtil<T> util;

    private NestedNodeInserter<T> inserter;

    private NestedNodeMover<T> mover;

    private NestedNodeRemover<T> remover;

    private NestedNodeRetriever<T> retriever;

    private NestedNodeRebuilder<T> rebuilder;

    public NestedNodeRepository() {
    }

    public NestedNodeRepository(EntityManager em) {
        this.em = em;
    }

    public void setNestedNodeUtil(NestedNodeUtil<T> util) {
        this.util = util;
    }

    public void setInserter(NestedNodeInserter<T> inserter) {
        this.inserter = inserter;
    }

    public void setMover(NestedNodeMover<T> mover) {
        this.mover = mover;
    }

    public void setRemover(NestedNodeRemover<T> remover) {
        this.remover = remover;
    }

    public void setRetriever(NestedNodeRetriever<T> retriever) {
        this.retriever = retriever;
    }

    public void setRebuilder(NestedNodeRebuilder<T> rebuilder) {
        this.rebuilder = rebuilder;
    }

    @Override
    public T insertAsFirstChildOf(T node, T parent) throws InvalidNodesHierarchyException {
        return this.insertOrMove(node, parent, NestedNodeHierarchyManipulator.MODE_FIRST_CHILD);
    }

    @Override
    public T insertAsLastChildOf(T node, T parent) throws InvalidNodesHierarchyException {
        return this.insertOrMove(node, parent, NestedNodeHierarchyManipulator.MODE_LAST_CHILD);
    }

    @Override
    public T insertAsNextSiblingOf(T node, T parent) throws InvalidNodesHierarchyException {
        return this.insertOrMove(node, parent, NestedNodeHierarchyManipulator.MODE_NEXT_SIBLING);
    }

    @Override
    public T insertAsPrevSiblingOf(T node, T parent) throws InvalidNodesHierarchyException {
        return this.insertOrMove(node, parent, NestedNodeHierarchyManipulator.MODE_PREV_SIBLING);
    }

    private T insertOrMove(T node, T parent, int mode) throws InvalidNodesHierarchyException {
        if (this.util.isNodeNew(node)) {
            return this.inserter.insert(node, parent, mode);
        } else {
            return this.mover.move(node, parent, mode);
        }
    }

    @Override
    public void removeSingle(T node) {
        this.remover.removeSingle(node);
    }

    @Override
    public void removeSubtree(T node) {
        this.remover.removeSubtree(node);
    }

    @Override
    public Iterable<T> getTreeAsList(T node) {
        return this.retriever.getTreeAsList(node);
    }

    @Override
    public Iterable<T> getChildren(T node) {
        return this.retriever.getChildren(node);
    }

    @Override
    public T getParent(T node) {
        return this.retriever.getParent(node);
    }

    @Override
    public Tree<T> getTree(T node) {
        return this.retriever.getTree(node);
    }

    @Override
    public Iterable<T> getParents(T node) {
        return this.retriever.getParents(node);
    }

    public void rebuildTree(Class<? extends NestedNode> nodeClass) throws InvalidNodesHierarchyException {
        this.rebuilder.rebuildTree(nodeClass);
    }
}
